package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.UserRegistrationAndDataDTO;
import com.example.multimedia.file_upload_api.dto.RoleUserRegistrationRequest;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class UserRegistrationAndDataService {

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private PanDetailsRepository panDetailsRepository;

    @Autowired
    private ChequeDetailsRepository chequeDetailsRepository;

    @Autowired
    private CertificateOfIncorporationRepository certificateOfIncorporationRepository;

    @Autowired
    private MsmeDetailsRepository msmeDetailsRepository;

    @Autowired
    private ItrDetailsRepository itrDetailsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public ServiceResponse processUserRegistrationAndData(UserRegistrationAndDataDTO dto) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Step 1: Get current super admin from security context
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Step 2: Get Authorization based on authKey first
            Authorization authorization = authorizationRepository.findByAuthKeyIgnoreCase(dto.getAuthKey())
                    .orElseThrow(() -> new RuntimeException("Invalid authKey. Must be one of: vendor, customer, super_admin"));

            // Step 2.5: Validate password
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Password is required"
                );
            }
            
            if (dto.getPassword().length() < 6) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Password must be at least 6 characters long"
                );
            }

            // Step 3: Check if email exists with the same role
            Optional<UserDetail> existingUser = userDetailRepository.findByEmail(dto.getEmail());
            if (existingUser.isPresent()) {
                // Check if user already has this role
                boolean hasRole = userAuthenticationRepository.existsByUserIdAndAuthKey(
                    existingUser.get(), 
                    authorization
                );
                
                if (hasRole) {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, 
                        AppConstants.ERRORCODE, 
                        "Registration failed: User already exists with role " + authorization.getAuthName()
                    );
                }
            }

            // Step 4: Check if GST number exists with the same role for the current super admin
            if (dto.getGstinNumber() != null && !dto.getGstinNumber().isEmpty()) {
                List<CompanyDetails> existingCompanies = companyDetailsRepository.findByGstinNumberAndSuperAdmin_SuperAdminIdAndAuthKey(
                    dto.getGstinNumber(), currentSuperAdmin.getSuperAdminId(), dto.getAuthKey());
                if (!existingCompanies.isEmpty()) {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, 
                        AppConstants.ERRORCODE, 
                        "Registration failed: GST number " + dto.getGstinNumber() + " already exists for role " + dto.getAuthKey() + " in your account"
                    );
                }
            }

            // Step 5: Create or get UserDetail
            UserDetail userDetail;
            if (existingUser.isPresent()) {
                userDetail = existingUser.get();
                // Update user details if provided
                if (dto.getFirstName() != null) userDetail.setFirstName(dto.getFirstName());
                if (dto.getLastName() != null) userDetail.setLastName(dto.getLastName());
                if (dto.getPhoneNumber() != null) userDetail.setPhoneNumber(dto.getPhoneNumber());
                // Update password with new one from DTO
                userDetail.setPassword(passwordEncoder.encode(dto.getPassword()));
                userDetail = userDetailRepository.save(userDetail);
            } else {
                userDetail = createUserDetail(dto, currentSuperAdmin);
                userDetail = userDetailRepository.save(userDetail);
            }

            // Step 6: Create UserAuthentication
            UserAuthentication userAuth = new UserAuthentication();
            userAuth.setUserId(userDetail.getUserId());
            userAuth.setAuthKey(String.valueOf(authorization.getAuthId()));
            userAuth.setIsActive(true);
            userAuthenticationRepository.save(userAuth);

            // Step 7: Create and save CompanyDetails
            CompanyDetails companyDetails = createCompanyDetails(dto, userDetail);
            companyDetails = companyDetailsRepository.save(companyDetails);

            // Step 8: Create and save PanDetails
            PanDetails panDetails = createPanDetails(dto, companyDetails);
            panDetailsRepository.save(panDetails);

            // Step 9: Create and save ChequeDetails
            ChequeDetails chequeDetails = createChequeDetails(dto, companyDetails);
            chequeDetailsRepository.save(chequeDetails);

            // Step 10: Create and save CertificateOfIncorporation if CIN number is provided
            if (dto.getCinNumber() != null && !dto.getCinNumber().isEmpty()) {
                CertificateOfIncorporation certificateOfIncorporation = createCertificateOfIncorporation(dto, companyDetails);
                certificateOfIncorporationRepository.save(certificateOfIncorporation);
            }

            // Prepare success response with user details
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userDetail.getUserId());
            userData.put("email", userDetail.getEmail());
            userData.put("firstName", userDetail.getFirstName());
            userData.put("lastName", userDetail.getLastName());
            userData.put("phoneNumber", userDetail.getPhoneNumber());
            
            // Add company details with file names
            Map<String, Object> companyData = new HashMap<>();
            companyData.put("companyId", companyDetails.getCompanyId());
            companyData.put("companyName", companyDetails.getCompanyName());
            companyData.put("gstinNumber", companyDetails.getGstinNumber());
            companyData.put("legalTradeName", companyDetails.getLegalTradeName());
            companyData.put("dateOfRegistration", companyDetails.getDateOfRegistration());
            companyData.put("typeOfRegistration", companyDetails.getTypeOfRegistration());
            companyData.put("panNumber", companyDetails.getPanNumber());
            companyData.put("authKey", companyDetails.getAuthKey());
            
            // Add file names
            Map<String, String> fileNames = new HashMap<>();
            fileNames.put("gstFileName", companyDetails.getGstFileName());
            fileNames.put("panFileName", companyDetails.getPanFileName());
            fileNames.put("chequeFileName", companyDetails.getChequeFileName());
            fileNames.put("coiFileName", companyDetails.getCoiFileName());
            companyData.put("fileNames", fileNames);
            
            // Add authorization info
            Map<String, Object> authData = new HashMap<>();
            authData.put("authId", authorization.getAuthId());
            authData.put("authKey", authorization.getAuthKey());
            authData.put("authName", authorization.getAuthName());
            userData.put("authorization", authData);
            
            userData.put("companyDetails", companyData);

            response.addData("user", userData);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response, 
                AppConstants.SUCCESSCODE, 
                "User registration completed successfully"
            );
            
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, 
                AppConstants.ERRORCODE, 
                "Registration failed: " + e.getMessage()
            );
        }
    }

    private UserDetail createUserDetail(UserRegistrationAndDataDTO dto, SuperAdmin superAdmin) {
        UserDetail userDetail = new UserDetail();
        userDetail.setSuperAdmin(superAdmin);
        userDetail.setEmail(dto.getEmail());
        userDetail.setPassword(passwordEncoder.encode(dto.getPassword())); // Use password from DTO
        userDetail.setFirstName(dto.getFirstName());
        userDetail.setLastName(dto.getLastName());
        userDetail.setPhoneNumber(dto.getPhoneNumber());
        userDetail.setIsActive(true);
        return userDetail;
    }

    private CompanyDetails createCompanyDetails(UserRegistrationAndDataDTO dto, UserDetail userDetail) {
        CompanyDetails companyDetails = new CompanyDetails();
        companyDetails.setCompanyName(dto.getLegalTradeName());
        companyDetails.setPanNumber(dto.getPanNumber());
        companyDetails.setUser(userDetail);
        companyDetails.setSuperAdmin(userDetail.getSuperAdmin());
        companyDetails.setGstinNumber(dto.getGstinNumber());
        companyDetails.setLegalTradeName(dto.getLegalTradeName());
        companyDetails.setDateOfRegistration(LocalDate.parse(dto.getDateOfRegistration(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        companyDetails.setPanTinCst(dto.getPanTinCst());
        companyDetails.setTypeOfRegistration(dto.getTypeOfRegistration());
        
        // Set file names
        companyDetails.setGstFileName(dto.getGstFileName());
        companyDetails.setPanFileName(dto.getPanFileName());
        companyDetails.setChequeFileName(dto.getChequeFileName());
        companyDetails.setCoiFileName(dto.getCoiFileName());
        
        // Set auth key
        companyDetails.setAuthKey(dto.getAuthKey());
        
        // Set company code to avoid validation failure
        String companyCode = dto.getGstinNumber();
        if (companyCode == null || companyCode.trim().isEmpty()) {
            companyCode = dto.getPanNumber();
        }
        if (companyCode == null || companyCode.trim().isEmpty()) {
            companyCode = "COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        companyDetails.setCompanyCode(companyCode);
        
        return companyDetails;
    }

    private PanDetails createPanDetails(UserRegistrationAndDataDTO dto, CompanyDetails companyDetails) {
        PanDetails panDetails = new PanDetails();
        panDetails.setCompany(companyDetails);
        panDetails.setPanNumber(dto.getPanNumber());
        panDetails.setName(dto.getName());
        panDetails.setDateOfBirthIncorporation(dto.getDateOfBirthIncorporation() != null ? dto.getDateOfBirthIncorporation().toString() : null);
        return panDetails;
    }

    private ChequeDetails createChequeDetails(UserRegistrationAndDataDTO dto, CompanyDetails companyDetails) {
        ChequeDetails chequeDetails = new ChequeDetails();
        chequeDetails.setCompany(companyDetails);
        chequeDetails.setAccountNumber(dto.getAccountNumber());
        chequeDetails.setIfsc(dto.getIfsc());
        chequeDetails.setBranch(dto.getBranch());
        chequeDetails.setBank(dto.getBank());
        chequeDetails.setCode(dto.getCode());
        return chequeDetails;
    }

    private CertificateOfIncorporation createCertificateOfIncorporation(UserRegistrationAndDataDTO dto, CompanyDetails companyDetails) {
        CertificateOfIncorporation certificateOfIncorporation = new CertificateOfIncorporation();
        certificateOfIncorporation.setCompany(companyDetails);
        certificateOfIncorporation.setCinNumber(dto.getCinNumber());
        return certificateOfIncorporation;
    }

    public ServiceResponse getCompleteVendorDetails(Long companyId, Long userId) {
        ServiceResponse response = new ServiceResponse();
        try {
            CompanyDetails company = null;
            
            // Resolve company based on role and arguments
            if (currentUserService.isCurrentUserSuperAdmin()) {
                if (companyId != null) {
                    company = companyDetailsRepository.findById(companyId)
                            .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
                } else if (userId != null) {
                    UserDetail user = userDetailRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                    company = user.getCompany();
                } else {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(
                            response, AppConstants.ERRORCODE, "companyId or userId parameter is required for admin role.");
                }
            } else {
                // Try regular user context
                UserDetail user = currentUserService.getCurrentUser();
                
                // If it's a vendor admin or super admin with normal user account
                if (companyId != null && user.getUserType() != null && 
                        ("ADMIN".equalsIgnoreCase(user.getUserType().name()) || "SUPER_ADMIN".equalsIgnoreCase(user.getUserType().name()))) {
                    company = companyDetailsRepository.findById(companyId)
                            .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
                } else {
                    company = user.getCompany();
                }
            }
            
            if (company == null) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "No company details found.");
            }

            Map<String, Object> completeDetails = new HashMap<>();
            
            // 1. Basic Company Details
            Map<String, Object> compMap = new HashMap<>();
            compMap.put("companyId", company.getCompanyId());
            compMap.put("companyName", company.getCompanyName());
            compMap.put("companyCode", company.getCompanyCode());
            compMap.put("status", company.getStatus());
            compMap.put("gstinNumber", company.getGstinNumber());
            compMap.put("legalTradeName", company.getLegalTradeName());
            compMap.put("registeredAddress", company.getRegisteredAddress());
            compMap.put("panNumber", company.getPanNumber());
            compMap.put("panTinCst", company.getPanTinCst());
            compMap.put("dateOfRegistration", company.getDateOfRegistration());
            compMap.put("typeOfRegistration", company.getTypeOfRegistration());
            compMap.put("gstFileName", company.getGstFileName());
            compMap.put("panFileName", company.getPanFileName());
            compMap.put("chequeFileName", company.getChequeFileName());
            compMap.put("coiFileName", company.getCoiFileName());
            completeDetails.put("companyDetails", compMap);

            // 2. GST Details
            Map<String, Object> gstMap = new HashMap<>();
            gstMap.put("gstinNumber", company.getGstinNumber());
            gstMap.put("legalTradeName", company.getLegalTradeName());
            gstMap.put("companyName", company.getCompanyName());
            gstMap.put("typeOfRegistration", company.getTypeOfRegistration());
            gstMap.put("dateOfRegistration", company.getDateOfRegistration());
            gstMap.put("registeredAddress", company.getRegisteredAddress());
            completeDetails.put("gst", gstMap);

            // 3. PAN Details
            PanDetails pan = company.getPanDetails();
            if (pan != null) {
                Map<String, Object> panMap = new HashMap<>();
                panMap.put("panNumber", pan.getPanNumber());
                panMap.put("name", pan.getName());
                panMap.put("dateOfBirthIncorporation", pan.getDateOfBirthIncorporation());
                panMap.put("category", pan.getCategory());
                panMap.put("fathersName", pan.getFathersName());
                completeDetails.put("pan", panMap);
            } else {
                completeDetails.put("pan", null);
            }

            // 4. Cheque Details
            ChequeDetails cheque = company.getChequeDetails();
            if (cheque != null) {
                Map<String, Object> chqMap = new HashMap<>();
                chqMap.put("accountNumber", cheque.getAccountNumber());
                chqMap.put("ifscCode", cheque.getIfsc());
                chqMap.put("bankName", cheque.getBank());
                chqMap.put("branch", cheque.getBranch());
                chqMap.put("signatory", cheque.getSignatory());
                chqMap.put("issuedTo", cheque.getIssuedTo());
                chqMap.put("issued", cheque.getIssued());
                chqMap.put("code", cheque.getCode());
                completeDetails.put("cheque", chqMap);
            } else {
                completeDetails.put("cheque", null);
            }

            // 5. COI Details
            CertificateOfIncorporation coi = company.getCertificateOfIncorporation();
            if (coi != null) {
                Map<String, Object> coiMap = new HashMap<>();
                coiMap.put("cinNumber", coi.getCinNumber());
                coiMap.put("businessName", coi.getBusinessName());
                coiMap.put("rocCode", coi.getRocCode());
                coiMap.put("registrationNumber", coi.getRegistrationNumber());
                coiMap.put("category", coi.getCategory());
                coiMap.put("subCategory", coi.getSubCategory());
                coiMap.put("companyClass", coi.getCompanyClass());
                coiMap.put("authorizedCapital", coi.getAuthorizedCapital());
                coiMap.put("paidCapital", coi.getPaidCapital());
                coiMap.put("incorporatedDate", coi.getIncorporatedDate());
                coiMap.put("email", coi.getEmail());
                coiMap.put("listed", coi.getListed());
                coiMap.put("lastAGMDate", coi.getLastAGMDate());
                coiMap.put("lastBSDate", coi.getLastBSDate());
                coiMap.put("active", coi.getActive());
                coiMap.put("status", coi.getStatus());
                coiMap.put("addressesJson", coi.getAddressesJson());
                coiMap.put("directorsJson", coi.getDirectorsJson());
                coiMap.put("chargesJson", coi.getChargesJson());
                coiMap.put("efilingsJson", coi.getEfilingsJson());
                completeDetails.put("coi", coiMap);
            } else {
                completeDetails.put("coi", null);
            }

            // 6. MSME Details
            MsmeDetails msme = company.getMsmeDetails();
            if (msme != null) {
                Map<String, Object> msmeMap = new HashMap<>();
                msmeMap.put("udyamNumber", msme.getUdyamNumber());
                msmeMap.put("entityName", msme.getEntityName());
                msmeMap.put("type", msme.getType());
                msmeMap.put("majorActivity", msme.getMajorActivity());
                msmeMap.put("gender", msme.getGender());
                msmeMap.put("socialCategory", msme.getSocialCategory());
                msmeMap.put("incorporatedDate", msme.getIncorporatedDate());
                msmeMap.put("commencedDate", msme.getCommencedDate());
                msmeMap.put("registeredDate", msme.getRegisteredDate());
                completeDetails.put("msme", msmeMap);
            } else {
                completeDetails.put("msme", null);
            }

            // 7. ITR Details
            ItrDetails itr = company.getItrDetails();
            if (itr != null) {
                Map<String, Object> itrMap = new HashMap<>();
                itrMap.put("pan", itr.getPan());
                itrMap.put("birthOrIncorporatedDate", itr.getBirthOrIncorporatedDate());
                itrMap.put("name", itr.getName());
                itrMap.put("fy", itr.getFy());
                itrMap.put("itrFiled", itr.getItrFiled());
                itrMap.put("itrType", itr.getItrType());
                itrMap.put("grossTurnover", itr.getGrossTurnover());
                itrMap.put("grossTurnoverFormatted", itr.getGrossTurnoverFormatted());
                itrMap.put("exportTurnover", itr.getExportTurnover());
                itrMap.put("exportTurnoverFormatted", itr.getExportTurnoverFormatted());
                itrMap.put("panStatus", itr.getPanStatus());
                completeDetails.put("itr", itrMap);
            } else {
                completeDetails.put("itr", null);
            }

            response.addData("vendorDetails", completeDetails);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "Vendor details retrieved successfully");
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Failed to retrieve vendor details: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public ServiceResponse registerRoleUser(RoleUserRegistrationRequest dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin from security context
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();

            // Validate email
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "Email is required");
            }

            Optional<UserDetail> existingUser = userDetailRepository.findByEmail(dto.getEmail());
            if (existingUser.isPresent()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "User already registered with this email");
            }

            // Get Authorization based on authKey
            Authorization authorization = authorizationRepository.findByAuthKeyIgnoreCase(dto.getAuthKey())
                    .orElseThrow(() -> new RuntimeException("Invalid authKey. Role not found: " + dto.getAuthKey()));

            // Determine UserType
            UserType userType;
            try {
                userType = UserType.valueOf(dto.getAuthKey().toUpperCase());
            } catch (IllegalArgumentException e) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "Invalid role key: " + dto.getAuthKey());
            }

            // Create UserDetail
            UserDetail userDetail = new UserDetail();
            userDetail.setSuperAdmin(currentSuperAdmin);
            userDetail.setEmail(dto.getEmail());
            String rawPassword = com.example.multimedia.file_upload_api.utils.PasswordUtils.generateRandomPassword(dto.getName());
            userDetail.setPassword(passwordEncoder.encode(rawPassword)); // Set generated password
            userDetail.setFirstName(dto.getName());
            userDetail.setLastName("");
            userDetail.setPhoneNumber(dto.getPhoneNumber());
            userDetail.setUserType(userType);
            userDetail.setIsActive(true);

            userDetail = userDetailRepository.save(userDetail);

            // Create UserAuthentication
            UserAuthentication userAuth = new UserAuthentication();
            userAuth.setUserId(userDetail.getUserId());
            userAuth.setAuthKey(String.valueOf(authorization.getAuthId()));
            userAuth.setIsActive(true);
            userAuthenticationRepository.save(userAuth);

            // Prepare success response
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userDetail.getUserId());
            userData.put("email", userDetail.getEmail());
            userData.put("password", rawPassword);
            userData.put("firstName", userDetail.getFirstName());
            userData.put("phoneNumber", userDetail.getPhoneNumber());
            userData.put("role", userDetail.getUserType().name());
            userData.put("superAdminId", currentSuperAdmin.getSuperAdminId());

            response.addData("user", userData);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "User registered successfully under your admin account");

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Registration failed: " + e.getMessage());
        }
    }
}
 