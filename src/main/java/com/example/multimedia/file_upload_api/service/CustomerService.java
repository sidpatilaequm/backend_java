package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
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
public class CustomerService {

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
    public ServiceResponse getAllCustomers() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();

            // Get all company details for customers belonging to current admin
            List<CompanyDetails> customerCompanies = companyDetailsRepository.findBySuperAdmin_SuperAdminIdAndAuthKey(currentAdminId, "customer");
            System.out.println("Found customer companies: " + customerCompanies.size());

            // Prepare response data
            List<Map<String, Object>> customerList = new ArrayList<>();
            for (CompanyDetails company : customerCompanies) {
                UserDetail user = company.getUser();
                
                // Only include active customers
                if (user == null || !user.getIsActive()) {
                    continue;
                }
                
                Map<String, Object> customerData = new HashMap<>();

                // Basic user info
                customerData.put("userId", user.getUserId());
                customerData.put("email", user.getEmail());
                customerData.put("firstName", user.getFirstName());
                customerData.put("lastName", user.getLastName());
                customerData.put("phoneNumber", user.getPhoneNumber());
                customerData.put("isActive", user.getIsActive());

                // Company info
                customerData.put("companyId", company.getCompanyId());
                customerData.put("companyName", company.getCompanyName());
                customerData.put("gstinNumber", company.getGstinNumber());
                customerData.put("legalTradeName", company.getLegalTradeName());
                customerData.put("registeredAddress", company.getRegisteredAddress());
                customerData.put("panNumber", company.getPanNumber());

                // Bank Details
                ChequeDetails chequeDetails = company.getChequeDetails();
                if (chequeDetails != null) {
                    Map<String, String> bankDetails = new HashMap<>();
                    bankDetails.put("accountNumber", chequeDetails.getAccountNumber());
                    bankDetails.put("bankName", chequeDetails.getBank());
                    bankDetails.put("branchName", chequeDetails.getBranch());
                    bankDetails.put("ifscCode", chequeDetails.getIfsc());
                    bankDetails.put("chequeCode", chequeDetails.getCode());
                    customerData.put("bankDetails", bankDetails);
                }

                // COI Details
                CertificateOfIncorporation coi = company.getCertificateOfIncorporation();
                if (coi != null) {
                    Map<String, String> coiDetails = new HashMap<>();
                    coiDetails.put("cinNumber", coi.getCinNumber());
                    coiDetails.put("createdDate", coi.getCreatedDate() != null ? coi.getCreatedDate().toString() : null);
                    customerData.put("coiDetails", coiDetails);
                }

                // File names
                Map<String, String> fileNames = new HashMap<>();
                fileNames.put("gstFileName", company.getGstFileName());
                fileNames.put("panFileName", company.getPanFileName());
                fileNames.put("chequeFileName", company.getChequeFileName());
                fileNames.put("coiFileName", company.getCoiFileName());
                customerData.put("fileNames", fileNames);

                customerList.add(customerData);
            }

            response.addData("customers", customerList);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Customers retrieved successfully"
            );

        } catch (Exception e) {
            e.printStackTrace(); // Add stack trace for debugging
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve customers: " + e.getMessage()
            );
        }
    }

    public List<CompanyDetails> getCustomers(Long userId) {
        // Get the customer authorization
        Authorization customerAuth = authorizationRepository.findByAuthKey("customer")
                .orElseThrow(() -> new RuntimeException("Customer authorization not found"));

        // Get all user authentications with customer role
        List<UserAuthentication> customerAuthentications = userAuthenticationRepository.findByAuthKey(customerAuth.getAuthKey());

        // Get all user IDs with customer role
        List<Long> customerUserIds = customerAuthentications.stream()
                .map(UserAuthentication::getUserId)
                .collect(Collectors.toList());

        // Get all company details for these users
        return companyDetailsRepository.findByUserUserIdIn(customerUserIds);
    }
} 