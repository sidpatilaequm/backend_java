package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.LocationDTO;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import com.example.multimedia.file_upload_api.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setup")
@CrossOrigin(origins = "*")
public class SetupController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/create-default-location")
    public ResponseEntity<?> createDefaultLocation() {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Check if user already has a location
            if (!locationService.getActiveLocations(superAdmin).isEmpty()) {
                return ResponseEntity.ok("Default location already exists for this user");
            }

            // Create default location
            LocationDTO defaultLocation = new LocationDTO();
            defaultLocation.setLocationName("Default Location");
            defaultLocation.setPinCode("000000");
            defaultLocation.setAddress("Default Address");
            defaultLocation.setCity("Default City");
            defaultLocation.setState("Default State");
            defaultLocation.setCountry("India");
            defaultLocation.setIsActive(true);

            LocationDTO createdLocation = locationService.createLocation(defaultLocation, superAdmin);
            return ResponseEntity.ok(createdLocation);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating default location: " + e.getMessage());
        }
    }

    @GetMapping("/check-setup")
    public ResponseEntity<?> checkSetup() {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            var locations = locationService.getActiveLocations(superAdmin);
            
            return ResponseEntity.ok("Setup check complete. Found " + locations.size() + " locations for current user.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Setup check failed: " + e.getMessage());
        }
    }

    @PostMapping("/fix-materials-location")
    public ResponseEntity<?> fixMaterialsLocation() {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Create default location if none exists
            if (locationService.getActiveLocations(superAdmin).isEmpty()) {
                LocationDTO defaultLocation = new LocationDTO();
                defaultLocation.setLocationName("Default Location");
                defaultLocation.setPinCode("000000");
                defaultLocation.setAddress("Default Address");
                defaultLocation.setCity("Default City");
                defaultLocation.setState("Default State");
                defaultLocation.setCountry("India");
                defaultLocation.setIsActive(true);

                locationService.createLocation(defaultLocation, superAdmin);
            }
            
            return ResponseEntity.ok("Materials location setup completed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fixing materials location: " + e.getMessage());
        }
    }
}
