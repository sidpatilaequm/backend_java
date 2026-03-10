package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.CompanyDetailsDTO;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorService {

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private CurrentUserService currentUserService;

    @Transactional
    public ServiceResponse getAllVendors() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();

            // Get all company details for vendors belonging to current admin
            List<CompanyDetails> vendorCompanies = companyDetailsRepository.findBySuperAdmin_SuperAdminIdAndAuthKey(currentAdminId, "vendor");
            System.out.println("Found vendor companies: " + vendorCompanies.size());

            // Prepare response data
            List<Map<String, Object>> vendorList = new ArrayList<>();
            for (CompanyDetails company : vendorCompanies) {
                UserDetail user = company.getUser();
                
                // Only include active vendors
                if (user == null || !user.getIsActive()) {
                    continue;
                }
                
                Map<String, Object> vendorData = new HashMap<>();

                // Basic user info
                vendorData.put("userId", user.getUserId());
                vendorData.put("email", user.getEmail());
                vendorData.put("firstName", user.getFirstName());
                vendorData.put("lastName", user.getLastName());
                vendorData.put("phoneNumber", user.getPhoneNumber());
                vendorData.put("isActive", user.getIsActive());

                // Company info
                vendorData.put("companyId", company.getCompanyId());
                vendorData.put("companyName", company.getCompanyName());
                vendorData.put("gstinNumber", company.getGstinNumber());
                vendorData.put("legalTradeName", company.getLegalTradeName());
                vendorData.put("registeredAddress", company.getRegisteredAddress());
                vendorData.put("panNumber", company.getPanNumber());

                // Bank Details
                ChequeDetails chequeDetails = company.getChequeDetails();
                if (chequeDetails != null) {
                    Map<String, String> bankDetails = new HashMap<>();
                    bankDetails.put("accountNumber", chequeDetails.getAccountNumber());
                    bankDetails.put("bankName", chequeDetails.getBank());
                    bankDetails.put("branchName", chequeDetails.getBranch());
                    bankDetails.put("ifscCode", chequeDetails.getIfsc());
                    bankDetails.put("chequeCode", chequeDetails.getCode());
                    vendorData.put("bankDetails", bankDetails);
                }

                // COI Details
                CertificateOfIncorporation coi = company.getCertificateOfIncorporation();
                if (coi != null) {
                    Map<String, String> coiDetails = new HashMap<>();
                    coiDetails.put("cinNumber", coi.getCinNumber());
                    coiDetails.put("createdDate", coi.getCreatedDate() != null ? coi.getCreatedDate().toString() : null);
                    vendorData.put("coiDetails", coiDetails);
                }

                // File names
                Map<String, String> fileNames = new HashMap<>();
                fileNames.put("gstFileName", company.getGstFileName());
                fileNames.put("panFileName", company.getPanFileName());
                fileNames.put("chequeFileName", company.getChequeFileName());
                fileNames.put("coiFileName", company.getCoiFileName());
                vendorData.put("fileNames", fileNames);

                vendorList.add(vendorData);
            }

            response.addData("vendors", vendorList);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Vendors retrieved successfully"
            );

        } catch (Exception e) {
            e.printStackTrace(); // Add stack trace for debugging
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve vendors: " + e.getMessage()
            );
        }
    }

    @Transactional
    public ServiceResponse updateVendorDetails(Long userId, CompanyDetailsDTO dto) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get user details
            UserDetail user = userDetailRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get user authentication
            UserAuthentication userAuth = userAuthenticationRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User authentication not found"));

            // If authKey is not provided, get it from user authentication
            String authKey = dto.getAuthKey();
            if (authKey == null || authKey.isEmpty()) {
                authKey = userAuth.getAuthKey();
            }

            // Get company details
            CompanyDetails company = companyDetailsRepository.findByUserUserId(userId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Company details not found"));

            // Update company details
            if (dto.getAuthKey() != null) company.setAuthKey(dto.getAuthKey());

            // Save updated company details
            company = companyDetailsRepository.save(company);

            // Prepare response
            Map<String, Object> vendorData = new HashMap<>();
            vendorData.put("companyId", company.getCompanyId());
            vendorData.put("authKey", company.getAuthKey());

            response.addData("vendor", vendorData);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Vendor details updated successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to update vendor details: " + e.getMessage()
            );
        }
    }

    public List<CompanyDetails> getVendors(Long userId) {
        // Get the vendor authorization
        Authorization vendorAuth = authorizationRepository.findByAuthKey("vendor")
                .orElseThrow(() -> new RuntimeException("Vendor authorization not found"));

        // Get all user authentications with vendor role
        List<UserAuthentication> vendorAuthentications = userAuthenticationRepository.findByAuthKey(vendorAuth.getAuthKey());

        // Get all user IDs with vendor role
        List<Long> vendorUserIds = vendorAuthentications.stream()
                .map(UserAuthentication::getUserId)
                .collect(Collectors.toList());

        // Get all company details for these users
        return companyDetailsRepository.findByUserUserIdIn(vendorUserIds);
    }
} 