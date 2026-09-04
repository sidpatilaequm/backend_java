package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.StorageBin;
import com.example.multimedia.file_upload_api.entity.StorageLocation;
import com.example.multimedia.file_upload_api.entity.Warehouse;
import com.example.multimedia.file_upload_api.repository.StorageBinRepository;
import com.example.multimedia.file_upload_api.repository.StorageLocationRepository;
import com.example.multimedia.file_upload_api.repository.WarehouseRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A warehouse-managed storage location, and the storage bins inside it. Warehouses and their
 * parent storage location are one-time master data, same as the rest of /api/mm/*; storage bins
 * are the one genuinely operational object here — a warehouse can hold thousands, created by
 * range rather than one at a time, so bin management gets its own idempotent add/range/preview/
 * delete surface instead of the plain list+create pattern the rest of this family uses. Adapted
 * from the standalone org-structure reference app's FastAPI bins router.
 */
@RestController
@RequestMapping("/api/mm/warehouses")
public class WarehouseController {

    private static final Pattern BIN_RE = Pattern.compile("^[A-Z0-9][A-Z0-9._/-]{0,9}$");
    private static final int MAX_RANGE = 50_000;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StorageLocationRepository storageLocationRepository;

    @Autowired
    private StorageBinRepository storageBinRepository;

    @Autowired
    private AdminAuthChecker adminAuthChecker;

    // ── Warehouses ───────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String plantCode) {
        List<Warehouse> warehouses = (plantCode == null || plantCode.isBlank())
                ? warehouseRepository.findAll()
                : warehouseRepository.findByPlantCode(plantCode.trim().toUpperCase());
        List<Map<String, Object>> out = warehouses.stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("total", out.size(), "warehouses", out));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        String warehouseNo = trimToNull(body.get("warehouseNo"));
        String description = trimToNull(body.get("description"));
        String plantCode = trimToNull(body.get("plantCode"));
        String slocId = trimToNull(body.get("slocId"));

        if (warehouseNo == null || warehouseNo.length() > 3) {
            return ResponseEntity.badRequest().body(Map.of("message", "warehouseNo is required and must be at most 3 characters."));
        }
        if (description == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "description is required."));
        }
        if (plantCode == null || slocId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "plantCode and slocId are required."));
        }

        StorageLocation.Pk slocKey = new StorageLocation.Pk();
        slocKey.setPlantCode(plantCode.toUpperCase());
        slocKey.setSlocId(slocId.toUpperCase());
        StorageLocation sloc = storageLocationRepository.findById(slocKey).orElse(null);
        if (sloc == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No storage location " + slocId + " on plant " + plantCode));
        }
        if (!sloc.isWarehouseManaged()) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "Storage location " + slocId + " is not warehouse managed — mark it warehouse managed first."));
        }

        warehouseNo = warehouseNo.toUpperCase();
        if (warehouseRepository.existsById(warehouseNo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Warehouse " + warehouseNo + " already exists."));
        }
        if (warehouseRepository.findByPlantCodeAndSlocId(sloc.getPlantCode(), sloc.getSlocId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Storage location " + slocId + " already has a warehouse."));
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseNo(warehouseNo);
        warehouse.setDescription(description);
        warehouse.setPlantCode(sloc.getPlantCode());
        warehouse.setSlocId(sloc.getSlocId());

        try {
            Warehouse saved = warehouseRepository.save(warehouse);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Warehouse " + warehouseNo + " already exists."));
        }
    }

    // ── Bins ─────────────────────────────────────────────────────────────

    @GetMapping("/{wh}/bins")
    public ResponseEntity<?> listBins(@PathVariable String wh,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(defaultValue = "0") int offset,
                                       @RequestParam(defaultValue = "200") int limit) {
        Warehouse warehouse = warehouseRepository.findById(wh.toUpperCase()).orElse(null);
        if (warehouse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "warehouse " + wh + " not found"));
        }
        int cappedLimit = Math.min(Math.max(limit, 1), 1000);
        int page = Math.max(offset, 0) / cappedLimit;
        PageRequest pageable = PageRequest.of(page, cappedLimit);
        Page<StorageBin> result = (search == null || search.isBlank())
                ? storageBinRepository.findByWarehouseNo(warehouse.getWarehouseNo(), pageable)
                : storageBinRepository.findByWarehouseNoAndBinCodeContainingIgnoreCase(warehouse.getWarehouseNo(), search.trim().toUpperCase(), pageable);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", result.getTotalElements());
        out.put("offset", offset);
        out.put("limit", cappedLimit);
        out.put("bins", result.getContent().stream().map(WarehouseController::binToMap).toList());
        return ResponseEntity.ok(out);
    }

    /** Add one bin or many. Bins already present are skipped, not rejected, so re-running a load is safe. */
    @PostMapping("/{wh}/bins")
    public ResponseEntity<?> addBins(@PathVariable String wh, @RequestBody Map<String, Object> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        Warehouse warehouse = warehouseRepository.findById(wh.toUpperCase()).orElse(null);
        if (warehouse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "warehouse " + wh + " not found"));
        }

        Object rawBins = body.get("bins");
        if (!(rawBins instanceof List<?> list) || list.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "bins must be a non-empty list."));
        }
        if (list.size() > MAX_RANGE) {
            return ResponseEntity.badRequest().body(Map.of("message", "a single request is limited to " + MAX_RANGE + " bins."));
        }

        List<BinSpec> specs = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) {
                return ResponseEntity.badRequest().body(Map.of("message", "each bin must be an object."));
            }
            String binCode = trimToNull(String.valueOf(m.get("binCode")));
            if (binCode == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "binCode is required for every bin."));
            }
            binCode = binCode.toUpperCase();
            if (!BIN_RE.matcher(binCode).matches()) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        binCode + " is not a valid bin code — 1 to 10 characters, starting with a letter or digit."));
            }
            String storageType = m.get("storageType") != null ? String.valueOf(m.get("storageType")) : "001";
            String storageSection = m.get("storageSection") != null ? String.valueOf(m.get("storageSection")) : "001";
            String binType = m.get("binType") != null ? String.valueOf(m.get("binType")) : null;
            specs.add(new BinSpec(binCode, storageType, storageSection, binType));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(insertBins(warehouse.getWarehouseNo(), specs));
    }

    /** Generate a sequential range, for example ARM00001 to ARM01500. */
    @PostMapping("/{wh}/bins/range")
    public ResponseEntity<?> addBinRange(@PathVariable String wh, @RequestBody Map<String, Object> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        Warehouse warehouse = warehouseRepository.findById(wh.toUpperCase()).orElse(null);
        if (warehouse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "warehouse " + wh + " not found"));
        }
        Object result = buildRange(body);
        if (result instanceof String errorMessage) {
            return ResponseEntity.badRequest().body(Map.of("message", errorMessage));
        }
        @SuppressWarnings("unchecked")
        List<BinSpec> specs = (List<BinSpec>) result;
        return ResponseEntity.status(HttpStatus.CREATED).body(insertBins(warehouse.getWarehouseNo(), specs));
    }

    /** Test run, mirroring SAP's LS05 test run — reports what would be created without writing anything. */
    @PostMapping("/{wh}/bins/range/preview")
    public ResponseEntity<?> previewBinRange(@PathVariable String wh, @RequestBody Map<String, Object> body) {
        Warehouse warehouse = warehouseRepository.findById(wh.toUpperCase()).orElse(null);
        if (warehouse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "warehouse " + wh + " not found"));
        }
        Object result = buildRange(body);
        if (result instanceof String errorMessage) {
            return ResponseEntity.badRequest().body(Map.of("message", errorMessage));
        }
        @SuppressWarnings("unchecked")
        List<BinSpec> specs = (List<BinSpec>) result;

        Set<String> wanted = new LinkedHashSet<>();
        for (BinSpec s : specs) wanted.add(s.binCode());
        Set<String> existing = storageBinRepository
                .findByWarehouseNoAndBinCodeIn(warehouse.getWarehouseNo(), wanted)
                .stream().map(StorageBin::getBinCode).collect(Collectors.toSet());
        long dup = wanted.stream().filter(existing::contains).count();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("requested", specs.size());
        out.put("created", wanted.size() - dup);
        out.put("skippedExisting", dup);
        out.put("totalNow", storageBinRepository.countByWarehouseNo(warehouse.getWarehouseNo()));
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{wh}/bins/{binCode}")
    public ResponseEntity<?> removeBin(@PathVariable String wh, @PathVariable String binCode) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        Warehouse warehouse = warehouseRepository.findById(wh.toUpperCase()).orElse(null);
        if (warehouse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "warehouse " + wh + " not found"));
        }
        StorageBin.Pk id = new StorageBin.Pk();
        id.setWarehouseNo(warehouse.getWarehouseNo());
        id.setBinCode(binCode.trim().toUpperCase());
        if (!storageBinRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "bin " + binCode + " not found"));
        }
        storageBinRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{wh}/bins")
    public ResponseEntity<?> clearBins(@PathVariable String wh, @RequestParam(defaultValue = "false") boolean confirm) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        Warehouse warehouse = warehouseRepository.findById(wh.toUpperCase()).orElse(null);
        if (warehouse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "warehouse " + wh + " not found"));
        }
        if (!confirm) {
            return ResponseEntity.badRequest().body(Map.of("message", "pass confirm=true to remove every bin in this warehouse"));
        }
        long deleted = storageBinRepository.deleteByWarehouseNo(warehouse.getWarehouseNo());
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /** Insert only the bins that don't already exist, collapsing duplicates within the payload first. */
    private Map<String, Object> insertBins(String warehouseNo, List<BinSpec> specs) {
        Map<String, BinSpec> wanted = new LinkedHashMap<>();
        for (BinSpec s : specs) wanted.putIfAbsent(s.binCode(), s);

        Set<String> existing = storageBinRepository
                .findByWarehouseNoAndBinCodeIn(warehouseNo, wanted.keySet())
                .stream().map(StorageBin::getBinCode).collect(Collectors.toSet());

        List<StorageBin> toInsert = new ArrayList<>();
        for (BinSpec s : wanted.values()) {
            if (existing.contains(s.binCode())) continue;
            StorageBin bin = new StorageBin();
            bin.setWarehouseNo(warehouseNo);
            bin.setBinCode(s.binCode());
            bin.setStorageType(s.storageType());
            bin.setStorageSection(s.storageSection());
            bin.setBinType(s.binType());
            toInsert.add(bin);
        }
        storageBinRepository.saveAll(toInsert);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("requested", specs.size());
        out.put("created", toInsert.size());
        out.put("skippedExisting", wanted.size() - toInsert.size());
        out.put("totalNow", storageBinRepository.countByWarehouseNo(warehouseNo));
        return out;
    }

    /** Returns a List&lt;BinSpec&gt; on success, or a String error message on validation failure. */
    private Object buildRange(Map<String, Object> body) {
        String prefix = trimToNull(String.valueOf(body.get("prefix")));
        if (prefix == null || prefix.length() > 6) {
            return "prefix is required and must be at most 6 characters.";
        }
        prefix = prefix.toUpperCase();

        Integer fromNo = toInt(body.get("fromNo"));
        Integer toNo = toInt(body.get("toNo"));
        if (fromNo == null || toNo == null || fromNo < 0 || toNo < 0) {
            return "fromNo and toNo are required and must be zero or greater.";
        }
        if (fromNo > toNo) {
            return "fromNo must be at or below toNo.";
        }
        if (toNo - fromNo + 1 > MAX_RANGE) {
            return "a single range is limited to " + MAX_RANGE + " bins.";
        }

        Integer width = toInt(body.get("width"));
        if (width == null) width = 5;
        if (width < 1 || width > 8) {
            return "width must be between 1 and 8.";
        }

        String storageType = body.get("storageType") != null ? String.valueOf(body.get("storageType")) : "001";
        String storageSection = body.get("storageSection") != null ? String.valueOf(body.get("storageSection")) : "001";
        String binType = body.get("binType") != null ? String.valueOf(body.get("binType")) : null;

        String sample = prefix + String.format("%0" + width + "d", toNo);
        if (sample.length() > 10) {
            return sample + " is " + sample.length() + " characters; the bin limit is 10.";
        }

        List<BinSpec> specs = new ArrayList<>();
        for (int n = fromNo; n <= toNo; n++) {
            String binCode = prefix + String.format("%0" + width + "d", n);
            if (!BIN_RE.matcher(binCode).matches()) {
                return binCode + " is not a valid bin code.";
            }
            specs.add(new BinSpec(binCode, storageType, storageSection, binType));
        }
        return specs;
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null || "null".equals(s)) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Map<String, Object> toMap(Warehouse w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("warehouseNo", w.getWarehouseNo());
        m.put("description", w.getDescription());
        m.put("plantCode", w.getPlantCode());
        m.put("slocId", w.getSlocId());
        m.put("binCount", storageBinRepository.countByWarehouseNo(w.getWarehouseNo()));
        return m;
    }

    private static Map<String, Object> binToMap(StorageBin b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("binCode", b.getBinCode());
        m.put("storageType", b.getStorageType());
        m.put("storageSection", b.getStorageSection());
        m.put("binType", b.getBinType());
        return m;
    }

    private record BinSpec(String binCode, String storageType, String storageSection, String binType) {}
}
