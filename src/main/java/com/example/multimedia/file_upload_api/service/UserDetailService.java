package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ForgotPasswordDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.UserDetailDTO;
import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class UserDetailService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    public Map<String, Object> registerUser(UserDetailDTO userDetailDTO) {
        // Validate SuperAdmin
        SuperAdmin superAdmin = superAdminRepository.findById(userDetailDTO.getSuperAdminId())
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found"));

        Optional<UserDetail> emailUser = userDetailRepository.findByEmail(userDetailDTO.getEmail());
        Optional<UserDetail> phoneUser = userDetailRepository.findByPhoneNumber(userDetailDTO.getPhoneNumber());

        UserDetail userDetail;

        // Case 1: Email or phone exists and belong to different users
        if (emailUser.isPresent() && phoneUser.isPresent() && !emailUser.get().getUserId().equals(phoneUser.get().getUserId())) {
            throw new RuntimeException("Email and phone number belong to different users.");
        }

        // Case 2: Use existing user if found
        if (emailUser.isPresent()) {
            userDetail = emailUser.get();
        } else if (phoneUser.isPresent()) {
            userDetail = phoneUser.get();
        } else {
            // Case 3: Create new user
            userDetail = new UserDetail();
            userDetail.setEmail(userDetailDTO.getEmail());
            
            // Generate random password
            String randomPassword = generateRandomPassword();
            userDetail.setPassword(randomPassword); // Store without encryption
            
            userDetail.setSignupDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            userDetail.setIsActive(true);
            
            // Set optional fields if provided
            if (userDetailDTO.getFirstName() != null) {
            userDetail.setFirstName(userDetailDTO.getFirstName());
            }
            if (userDetailDTO.getLastName() != null) {
            userDetail.setLastName(userDetailDTO.getLastName());
            }
            if (userDetailDTO.getPhoneNumber() != null) {
            userDetail.setPhoneNumber(userDetailDTO.getPhoneNumber());
            }
            
            userDetail.setSuperAdmin(superAdmin);
            userDetail = userDetailRepository.save(userDetail);
        }

        // Get Authorization
        Authorization authorization = authorizationRepository.findByAuthKey(userDetailDTO.getAuthKey())
                .orElseThrow(() -> new RuntimeException("Invalid authKey"));

        // Check if user already has this role
        boolean roleExists = userAuthenticationRepository.existsByUserIdAndAuthKey(userDetail, authorization);
        if (roleExists) {
            throw new RuntimeException("User already registered with this role.");
        }

        // Save new role mapping
        UserAuthentication userAuth = new UserAuthentication();
        userAuth.setUserId(userDetail.getUserId());
        userAuth.setAuthKey(authorization.getAuthKey());
        userAuth.setIsActive(true);
        userAuthenticationRepository.save(userAuth);

        // Response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userDetail.getUserId());
        response.put("email", userDetail.getEmail());
        response.put("firstName", userDetail.getFirstName());
        response.put("lastName", userDetail.getLastName());
        response.put("phoneNumber", userDetail.getPhoneNumber());
        response.put("isActive", userDetail.getIsActive());
        response.put("authId", authorization.getAuthId());
        response.put("superAdminId", superAdmin.getSuperAdminId());
        response.put("password", userDetail.getPassword()); // Include the generated password in response
        response.put("message", "User registered successfully with role: " + authorization.getAuthName());

        return response;
    }

    public UserDetail getUserById(Long userId) {
        return userDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDetail updateUser(Long userId, UserDetailDTO userDetailDTO) {
        UserDetail existingUser = getUserById(userId);
        
        if (userDetailDTO.getFirstName() != null) {
        existingUser.setFirstName(userDetailDTO.getFirstName());
        }
        if (userDetailDTO.getLastName() != null) {
        existingUser.setLastName(userDetailDTO.getLastName());
        }
        if (userDetailDTO.getPhoneNumber() != null) {
        existingUser.setPhoneNumber(userDetailDTO.getPhoneNumber());
        }
        
        if (userDetailDTO.getPassword() != null && !userDetailDTO.getPassword().isEmpty()) {
            existingUser.setPassword(userDetailDTO.getPassword()); // Store without encryption
        }

        return userDetailRepository.save(existingUser);
    }

    public void deactivateUser(Long userId) {
        UserDetail user = getUserById(userId);
        user.setIsActive(false);
        userDetailRepository.save(user);
    }

    public ServiceResponse resetPasswordByEmail(ForgotPasswordDTO dto) {
        ServiceResponse response = new ServiceResponse();

        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            return serviceControllerUtils.prepareMobileResponseInvalidData(response, "No user found with the provided email.");
        }

        UserDetail user = userOpt.get();
        user.setPassword(dto.getNewPassword()); // Store without encryption
        userDetailRepository.save(user);

        return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response, AppConstants.SUCCESSCODE, "Password updated successfully.");
    }
} 