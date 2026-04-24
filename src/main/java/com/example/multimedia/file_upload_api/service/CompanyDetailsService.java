package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.CompanyDetailsDTO;
import com.example.multimedia.file_upload_api.dto.CountryDTO;
import com.example.multimedia.file_upload_api.dto.CurrencyDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.Country;
import com.example.multimedia.file_upload_api.entity.Currency;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyDetailsService {

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public ServiceResponse getAllCompanies() {
        ServiceResponse response = new ServiceResponse();
        try {
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            List<CompanyDetails> companies = companyDetailsRepository.findBySuperAdminSuperAdminId(currentAdminId);
            List<CompanyDetailsDTO> dtos = companies.stream().map(this::convertToDTO).collect(Collectors.toList());
            response.setStatus("SUCCESS");
            response.setStatusMsg("Companies retrieved successfully");
            response.addData("companies", dtos);
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve companies: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse getCompanyById(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            Optional<CompanyDetails> company = companyDetailsRepository.findById(id);
            if (company.isPresent() && company.get().getSuperAdmin().getSuperAdminId().equals(currentAdminId)) {
                response.setStatus("SUCCESS");
                response.setStatusMsg("Company retrieved successfully");
                response.addData("company", convertToDTO(company.get()));
            } else {
                response.setStatus("ERROR");
                response.setStatusMsg("Company not found");
            }
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve company: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse saveCompany(CompanyDetails company) {
        ServiceResponse response = new ServiceResponse();
        try {
            SuperAdmin currentAdmin = currentUserService.getCurrentSuperAdmin();

            // Check for duplicates for the same admin
            if (companyDetailsRepository.existsByCompanyCodeAndSuperAdmin_SuperAdminId(company.getCompanyCode(),
                    currentAdmin.getSuperAdminId())) {
                Optional<CompanyDetails> existing = companyDetailsRepository
                        .findByCompanyCodeAndSuperAdmin_SuperAdminId(company.getCompanyCode(),
                                currentAdmin.getSuperAdminId());
                if (existing.isPresent() && !existing.get().getCompanyId().equals(company.getCompanyId())) {
                    response.setStatus("ERROR");
                    response.setStatusMsg("Company with this code already exists for your account");
                    return response;
                }
            }

            company.setSuperAdmin(currentAdmin);
            CompanyDetails savedCompany = companyDetailsRepository.save(company);
            response.setStatus("SUCCESS");
            response.setStatusMsg("Company saved successfully");
            response.addData("company", convertToDTO(savedCompany));
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to save company: " + e.getMessage());
        }
        return response;
    }

    public ServiceResponse deleteCompany(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            companyDetailsRepository.deleteById(id);
            response.setStatus("SUCCESS");
            response.setStatusMsg("Company deleted successfully");
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to delete company: " + e.getMessage());
        }
        return response;
    }

    private CompanyDetailsDTO convertToDTO(CompanyDetails company) {
        CompanyDetailsDTO dto = new CompanyDetailsDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyCode(company.getCompanyCode());
        dto.setCompanyName(company.getCompanyName());
        dto.setStatus(company.getStatus());
        dto.setGstinNumber(company.getGstinNumber());
        dto.setLegalTradeName(company.getLegalTradeName());
        dto.setRegisteredAddress(company.getRegisteredAddress());
        dto.setPanNumber(company.getPanNumber());
        dto.setPanTinCst(company.getPanTinCst());
        dto.setDateOfRegistration(
                company.getDateOfRegistration() != null ? company.getDateOfRegistration().atStartOfDay() : null);
        dto.setTypeOfRegistration(company.getTypeOfRegistration());
        dto.setAuthKey(company.getAuthKey());
        dto.setGstFileName(company.getGstFileName());
        dto.setPanFileName(company.getPanFileName());
        dto.setChequeFileName(company.getChequeFileName());
        dto.setCoiFileName(company.getCoiFileName());

        if (company.getCountry() != null) {
            dto.setCountry(convertCountryToDTO(company.getCountry()));
        }
        if (company.getCurrency() != null) {
            dto.setCurrency(convertCurrencyToDTO(company.getCurrency()));
        }

        return dto;
    }

    private CountryDTO convertCountryToDTO(Country country) {
        return new CountryDTO(
                country.getCountryId(),
                country.getCountryName(),
                country.getIsoCode(),
                country.getPhoneCode(),
                country.getStatus());
    }

    private CurrencyDTO convertCurrencyToDTO(Currency currency) {
        return new CurrencyDTO(
                currency.getCurrencyId(),
                currency.getCurrencyCode(),
                currency.getCurrencyName(),
                currency.getSymbol(),
                currency.getStatus());
    }
}
