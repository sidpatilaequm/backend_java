package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.CountryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Country;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public ServiceResponse getAllCountries() {
        ServiceResponse response = new ServiceResponse();
        try {
            SuperAdmin currentAdmin = currentUserService.getCurrentSuperAdmin();
            List<Country> countries = countryRepository.findBySuperAdmin(currentAdmin);
            List<CountryDTO> dtos = countries.stream().map(this::convertToDTO).collect(Collectors.toList());
            response.setStatus("SUCCESS");
            response.setStatusMsg("Countries retrieved successfully");
            response.addData("countries", dtos);
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve countries: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse getCountryById(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            SuperAdmin currentAdmin = currentUserService.getCurrentSuperAdmin();
            Optional<Country> country = countryRepository.findById(id);
            if (country.isPresent()
                    && country.get().getSuperAdmin().getSuperAdminId().equals(currentAdmin.getSuperAdminId())) {
                response.setStatus("SUCCESS");
                response.setStatusMsg("Country retrieved successfully");
                response.addData("country", convertToDTO(country.get()));
            } else {
                response.setStatus("ERROR");
                response.setStatusMsg("Country not found");
            }
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve country: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse saveCountry(Country country) {
        ServiceResponse response = new ServiceResponse();
        try {
            SuperAdmin currentAdmin = currentUserService.getCurrentSuperAdmin();

            // Check for duplicates for the same admin
            if (countryRepository.existsByCountryNameAndSuperAdmin(country.getCountryName(), currentAdmin)) {
                Optional<Country> existing = countryRepository.findByCountryNameAndSuperAdmin(country.getCountryName(),
                        currentAdmin);
                if (existing.isPresent() && !existing.get().getCountryId().equals(country.getCountryId())) {
                    response.setStatus("ERROR");
                    response.setStatusMsg("Country with this name already exists for your account");
                    return response;
                }
            }

            country.setSuperAdmin(currentAdmin);
            Country savedCountry = countryRepository.save(country);
            response.setStatus("SUCCESS");
            response.setStatusMsg("Country saved successfully");
            response.addData("country", convertToDTO(savedCountry));
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to save country: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse deleteCountry(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            countryRepository.deleteById(id);
            response.setStatus("SUCCESS");
            response.setStatusMsg("Country deleted successfully");
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to delete country: " + e.getMessage());
        }
        return response;
    }

    private CountryDTO convertToDTO(Country country) {
        return new CountryDTO(
                country.getCountryId(),
                country.getCountryName(),
                country.getIsoCode(),
                country.getPhoneCode(),
                country.getStatus());
    }
}
