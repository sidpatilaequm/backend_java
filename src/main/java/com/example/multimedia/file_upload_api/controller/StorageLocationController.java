package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Plant;
import com.example.multimedia.file_upload_api.entity.StorageLocation;
import com.example.multimedia.file_upload_api.repository.PlantRepository;
import com.example.multimedia.file_upload_api.repository.StorageLocationRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A plant's inventory object (T001L). See StorageLocation.java's javadoc for why this is a
 * different thing from PlantLocation. Only a storage location with is_warehouse_managed=true can
 * carry a Warehouse — enforced at the database level (see the 01_schema.sql this was adapted
 * from), not here.
 */
@RestController
@RequestMapping("/api/mm/storage-locations")
public class StorageLocationController {

    @Autowired
    private StorageLocationRepository storageLocationRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private AdminAuthChecker adminAuthChecker;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String plantCode) {
        List<StorageLocation> locations = (plantCode == null || plantCode.isBlank())
                ? storageLocationRepository.findAll()
                : storageLocationRepository.findByPlantCode(plantCode.trim().toUpperCase());
        List<Map<String, Object>> out = locations.stream().map(StorageLocationController::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("total", out.size(), "storageLocations", out));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        String plantCode = trimToNull((String) body.get("plantCode"));
        String slocId = trimToNull((String) body.get("slocId"));
        String description = trimToNull((String) body.get("description"));
        boolean warehouseManaged = Boolean.TRUE.equals(body.get("isWarehouseManaged"));

        if (plantCode == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "plantCode is required."));
        }
        if (slocId == null || slocId.length() != 4) {
            return ResponseEntity.badRequest().body(Map.of("message", "slocId is required and must be exactly 4 characters."));
        }
        if (description == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "description is required."));
        }

        Plant plant = plantRepository.findById(plantCode.toUpperCase()).orElse(null);
        if (plant == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No plant with code " + plantCode));
        }

        StorageLocation.Pk id = new StorageLocation.Pk();
        id.setPlantCode(plant.getPlantCode());
        id.setSlocId(slocId.toUpperCase());
        if (storageLocationRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Storage location " + slocId + " already exists on plant " + plantCode));
        }

        StorageLocation location = new StorageLocation();
        location.setPlantCode(plant.getPlantCode());
        location.setSlocId(slocId.toUpperCase());
        location.setDescription(description);
        location.setWarehouseManaged(warehouseManaged);

        try {
            StorageLocation saved = storageLocationRepository.save(location);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Storage location " + slocId + " already exists on plant " + plantCode));
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Map<String, Object> toMap(StorageLocation l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("plantCode", l.getPlantCode());
        m.put("slocId", l.getSlocId());
        m.put("description", l.getDescription());
        m.put("isWarehouseManaged", l.isWarehouseManaged());
        return m;
    }
}
