package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.UserRegistrationAndDataDTO;
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
} 