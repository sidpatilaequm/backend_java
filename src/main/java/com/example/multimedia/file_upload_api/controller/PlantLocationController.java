package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Plant;
import com.example.multimedia.file_upload_api.entity.PlantLocation;
import com.example.multimedia.file_upload_api.repository.PlantLocationRepository;
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
import java.util.stream.Collectors;

/**
 * A plant's maintenance object (T499S) — Shop Floor, Utility, Infrastructure. See
 * PlantLocation.java's javadoc for why this is a different thing from StorageLocation.
 */
@RestController
@RequestMapping("/api/mm/plant-locations")
public class PlantLocationController {

    @Autowired
    private PlantLocationRepository plantLocationRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private AdminAuthChecker adminAuthChecker;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String plantCode) {
        List<PlantLocation> locations = (plantCode == null || plantCode.isBlank())
                ? plantLocationRepository.findAll()
                : plantLocationRepository.findByPlantCode(plantCode.trim().toUpperCase());
        List<Map<String, String>> out = locations.stream().map(PlantLocationController::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("total", out.size(), "plantLocations", out));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        if (!adminAuthChecker.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required."));
        }
        String plantCode = trimToNull(body.get("plantCode"));
        String locationId = trimToNull(body.get("locationId"));
        String name = trimToNull(body.get("name"));

        if (plantCode == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "plantCode is required."));
        }
        if (locationId == null || locationId.length() > 10) {
            return ResponseEntity.badRequest().body(Map.of("message", "locationId is required and must be at most 10 characters."));
        }
        if (name == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "name is required."));
        }

        Plant plant = plantRepository.findById(plantCode.toUpperCase()).orElse(null);
        if (plant == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No plant with code " + plantCode));
        }

        PlantLocation.Pk id = new PlantLocation.Pk();
        id.setPlantCode(plant.getPlantCode());
        id.setLocationId(locationId.toUpperCase());
        if (plantLocationRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Location " + locationId + " already exists on plant " + plantCode));
        }

        PlantLocation location = new PlantLocation();
        location.setPlantCode(plant.getPlantCode());
        location.setLocationId(locationId.toUpperCase());
        location.setName(name);

        try {
            PlantLocation saved = plantLocationRepository.save(location);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Location " + locationId + " already exists on plant " + plantCode));
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Map<String, String> toMap(PlantLocation l) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("plantCode", l.getPlantCode());
        m.put("locationId", l.getLocationId());
        m.put("name", l.getName());
        return m;
    }
}
