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

    @Autowired
    private VendorMasterRepository vendorMasterRepository;

    @Transactional
    public ServiceResponse getAllVendors() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current admin ID for filtering
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();

            // Get all VendorMaster records
            List<VendorMaster> vendorMasters = vendorMasterRepository.findAll();
            System.out.println("Found vendor masters: " + vendorMasters.size());

            // Prepare response data
            List<Map<String, Object>> vendorList = new ArrayList<>();
            for (VendorMaster vm : vendorMasters) {
                if (vm.getEmail() == null || vm.getEmail().isEmpty()) {
                    continue;
                }

                // Find UserDetail linked to this VendorMaster via email
                Optional<UserDetail> userOpt = userDetailRepository.findByEmail(vm.getEmail());
                if (userOpt.isEmpty()) {
                    continue; // Skip if no user linked
                }

                UserDetail user = userOpt.get();

                // Ensure this user belongs to the current super admin
                if (user.getSuperAdmin() == null || !user.getSuperAdmin().getSuperAdminId().equals(currentAdminId)) {
                    continue;
                }

                Map<String, Object> vendorData = new HashMap<>();

                // User details
                vendorData.put("userId", user.getUserId());
                vendorData.put("email", user.getEmail());
                vendorData.put("firstName", user.getFirstName());
                vendorData.put("lastName", user.getLastName());
                vendorData.put("phoneNumber", user.getPhoneNumber());
                vendorData.put("isActive", user.getIsActive());

                if (user.getCompany() != null) {
                    vendorData.put("companyId", user.getCompany().getCompanyId());
                }

                // Vendor Master details
                vendorData.put("vendorId", vm.getVendorId());
                vendorData.put("bpNo", vm.getBpNo());
                vendorData.put("companyName", vm.getName()); // mapping name to companyName to keep API compatible
                vendorData.put("name", vm.getName());
                vendorData.put("gstNumber", vm.getGstNumber());
                vendorData.put("cityName", vm.getCityName());
                vendorData.put("streetAndHouseNumber", vm.getStreetAndHouseNumber());
                vendorData.put("streetName1", vm.getStreetName1());
                vendorData.put("postalCode", vm.getPostalCode());
                vendorData.put("countryCode", vm.getCountryCode());
                vendorData.put("bankAccountNumber", vm.getBankAccountNumber());

                vendorList.add(vendorData);
            }

            response.addData("vendors", vendorList);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response,
                    AppConstants.SUCCESSCODE,
                    "Vendors retrieved successfully");

        } catch (Exception e) {
            e.printStackTrace(); // Add stack trace for debugging
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Failed to retrieve vendors: " + e.getMessage());
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
            if (dto.getAuthKey() != null)
                company.setAuthKey(dto.getAuthKey());

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
                    "Vendor details updated successfully");

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Failed to update vendor details: " + e.getMessage());
        }
    }

    public List<CompanyDetails> getVendors(Long userId) {
        // Get the vendor authorization
        Authorization vendorAuth = authorizationRepository.findByAuthKey("vendor")
                .orElseThrow(() -> new RuntimeException("Vendor authorization not found"));

        // Get all user authentications with vendor role
        List<UserAuthentication> vendorAuthentications = userAuthenticationRepository
                .findByAuthKey(vendorAuth.getAuthKey());

        // Get all user IDs with vendor role
        List<Long> vendorUserIds = vendorAuthentications.stream()
                .map(UserAuthentication::getUserId)
                .collect(Collectors.toList());

        // Get all company details for these users
        return companyDetailsRepository.findByUserUserIdIn(vendorUserIds);
    }
}