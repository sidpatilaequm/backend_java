package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.LocationDTO;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.service.LocationService;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<?> createLocation(@RequestBody LocationDTO locationDTO) {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            LocationDTO createdLocation = locationService.createLocation(locationDTO, superAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating location: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLocations() {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            List<LocationDTO> locations = locationService.getAllLocations(superAdmin);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching locations: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveLocations() {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            List<LocationDTO> locations = locationService.getActiveLocations(superAdmin);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching active locations: " + e.getMessage());
        }
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<?> getLocationById(@PathVariable Long locationId) {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            LocationDTO location = locationService.getLocationById(locationId, superAdmin);
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error fetching location: " + e.getMessage());
        }
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<?> updateLocation(@PathVariable Long locationId, @RequestBody LocationDTO locationDTO) {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            LocationDTO updatedLocation = locationService.updateLocation(locationId, locationDTO, superAdmin);
            return ResponseEntity.ok(updatedLocation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating location: " + e.getMessage());
        }
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long locationId) {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            locationService.deleteLocation(locationId, superAdmin);
            return ResponseEntity.ok("Location deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting location: " + e.getMessage());
        }
    }

    @PutMapping("/{locationId}/soft-delete")
    public ResponseEntity<?> softDeleteLocation(@PathVariable Long locationId) {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            locationService.softDeleteLocation(locationId, superAdmin);
            return ResponseEntity.ok("Location deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deactivating location: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchLocations(@RequestParam String searchTerm) {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            List<LocationDTO> locations = locationService.searchLocations(searchTerm, superAdmin);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error searching locations: " + e.getMessage());
        }
    }

    @GetMapping("/by-admin")
    public ResponseEntity<?> getLocationsBySuperAdmin() {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            List<LocationDTO> locations = locationService.getAllLocations(superAdmin);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching locations by admin: " + e.getMessage());
        }
    }
}
