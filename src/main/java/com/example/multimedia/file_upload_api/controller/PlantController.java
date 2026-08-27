package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Company;
import com.example.multimedia.file_upload_api.entity.Plant;
import com.example.multimedia.file_upload_api.repository.CompanyRepository;
import com.example.multimedia.file_upload_api.repository.PlantRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** A physical site under one Company. See Company.java's javadoc for the enterprise-structure design. */
@RestController
@RequestMapping("/api/mm/plants")
public class PlantController {

    private static final Pattern GST_PATTERN =
            Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$");

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AdminAuthChecker adminAuthChecker;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String companyCode) {
        List<Plant> plants = (companyCode == null || companyCode.isBlank())
                ? plantRepository.findAll()
                : plantRepository.findByCompany_CompanyCode(companyCode.trim().toUpperCase());
        List<Map<String, String>> out = plants.stream().map(PlantController::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("total", out.size(), "plants", out));
    }

    @GetMapping("/{plantCode}")
    public ResponseEntity<?> getByCode(@PathVariable String plantCode) {
        return plantRepository.findById(plantCode.toUpperCase())
                .map(p -> ResponseEntity.ok(toMap(p)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No plant with code " + plantCode)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        String plantCode = trimToNull(body.get("plantCode"));
        String plantName = trimToNull(body.get("plantName"));
        String gstNumber = trimToNull(body.get("gstNumber"));
        String companyCode = trimToNull(body.get("companyCode"));

        if (plantCode == null || plantCode.length() > 4) {
            return ResponseEntity.badRequest().body(Map.of("message", "plantCode is required and must be at most 4 characters."));
        }
        if (plantName == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "plantName is required."));
        }
        if (companyCode == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "companyCode is required."));
        }
        if (gstNumber != null && !GST_PATTERN.matcher(gstNumber).matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", "gstNumber is not a valid 15-character GSTIN."));
        }

        Company company = companyRepository.findById(companyCode.toUpperCase()).orElse(null);
        if (company == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No company with code " + companyCode));
        }

        plantCode = plantCode.toUpperCase();
        // save() upserts for an assigned (non-generated) @Id — check first, see CompanyController.
        if (plantRepository.existsById(plantCode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Plant code " + plantCode + " already exists."));
        }

        Plant plant = new Plant();
        plant.setPlantCode(plantCode);
        plant.setPlantName(plantName);
        plant.setGstNumber(gstNumber);
        plant.setCompany(company);

        try {
            Plant saved = plantRepository.save(plant);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Plant code " + plantCode + " already exists."));
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Map<String, String> toMap(Plant p) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("plantCode", p.getPlantCode());
        m.put("plantName", p.getPlantName());
        m.put("gstNumber", p.getGstNumber());
        m.put("companyCode", p.getCompany() != null ? p.getCompany().getCompanyCode() : null);
        return m;
    }
}
