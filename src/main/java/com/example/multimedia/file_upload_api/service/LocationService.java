package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.LocationDTO;
import com.example.multimedia.file_upload_api.entity.Location;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public LocationDTO createLocation(LocationDTO locationDTO, SuperAdmin superAdmin) {
        // Check if location name already exists for this super admin
        if (locationRepository.existsByLocationNameAndSuperAdmin(locationDTO.getLocationName(), superAdmin)) {
            throw new RuntimeException("Location with name '" + locationDTO.getLocationName() + "' already exists");
        }

        Location location = new Location();
        location.setLocationName(locationDTO.getLocationName());
        location.setPinCode(locationDTO.getPinCode());
        location.setAddress(locationDTO.getAddress());
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setCountry(locationDTO.getCountry());
        location.setIsActive(locationDTO.getIsActive() != null ? locationDTO.getIsActive() : true);
        location.setSuperAdmin(superAdmin);

        Location savedLocation = locationRepository.save(location);
        return convertToDTO(savedLocation);
    }

    public LocationDTO updateLocation(Long locationId, LocationDTO locationDTO, SuperAdmin superAdmin) {
        Location location = locationRepository.findByLocationIdAndSuperAdmin(locationId, superAdmin)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));

        // Check if location name already exists for this super admin (excluding current location)
        if (!location.getLocationName().equals(locationDTO.getLocationName()) &&
            locationRepository.existsByLocationNameAndSuperAdmin(locationDTO.getLocationName(), superAdmin)) {
            throw new RuntimeException("Location with name '" + locationDTO.getLocationName() + "' already exists");
        }

        location.setLocationName(locationDTO.getLocationName());
        location.setPinCode(locationDTO.getPinCode());
        location.setAddress(locationDTO.getAddress());
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setCountry(locationDTO.getCountry());
        if (locationDTO.getIsActive() != null) {
            location.setIsActive(locationDTO.getIsActive());
        }

        Location updatedLocation = locationRepository.save(location);
        return convertToDTO(updatedLocation);
    }

    public LocationDTO getLocationById(Long locationId, SuperAdmin superAdmin) {
        Location location = locationRepository.findByLocationIdAndSuperAdmin(locationId, superAdmin)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
        return convertToDTO(location);
    }

    public List<LocationDTO> getAllLocations(SuperAdmin superAdmin) {
        List<Location> locations = locationRepository.findBySuperAdmin(superAdmin);
        return locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LocationDTO> getActiveLocations(SuperAdmin superAdmin) {
        List<Location> locations = locationRepository.findActiveLocationsBySuperAdmin(superAdmin);
        return locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LocationDTO> searchLocations(String searchTerm, SuperAdmin superAdmin) {
        List<Location> locations = locationRepository.searchActiveLocationsBySuperAdmin(superAdmin, searchTerm);
        return locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteLocation(Long locationId, SuperAdmin superAdmin) {
        Location location = locationRepository.findByLocationIdAndSuperAdmin(locationId, superAdmin)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));

        // Check if location has materials
        if (location.getMaterials() != null && !location.getMaterials().isEmpty()) {
            throw new RuntimeException("Cannot delete location. It has associated materials. Please reassign or delete materials first.");
        }

        locationRepository.delete(location);
    }

    public void softDeleteLocation(Long locationId, SuperAdmin superAdmin) {
        Location location = locationRepository.findByLocationIdAndSuperAdmin(locationId, superAdmin)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));

        location.setIsActive(false);
        locationRepository.save(location);
    }

    public LocationDTO convertToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setLocationId(location.getLocationId());
        dto.setLocationName(location.getLocationName());
        dto.setPinCode(location.getPinCode());
        dto.setAddress(location.getAddress());
        dto.setCity(location.getCity());
        dto.setState(location.getState());
        dto.setCountry(location.getCountry());
        dto.setIsActive(location.getIsActive());
        dto.setCreatedDate(location.getCreatedDate());
        dto.setModifiedDate(location.getModifiedDate());
        return dto;
    }

    public Location getLocationEntity(Long locationId, SuperAdmin superAdmin) {
        return locationRepository.findByLocationIdAndSuperAdmin(locationId, superAdmin)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
    }

    public Location getLocationEntityByName(String locationName, SuperAdmin superAdmin) {
        // Use trimmed query to handle whitespace issues
        return locationRepository.findByLocationNameTrimmedIgnoreCaseAndSuperAdminAndIsActiveTrue(locationName.trim(), superAdmin)
                .orElseThrow(() -> new RuntimeException("Location not found with name: '" + locationName + "'. Please check the location name and ensure it exists and is active."));
    }
}
