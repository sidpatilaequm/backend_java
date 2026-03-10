package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ForgotPasswordDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.UserDetailDTO;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.service.UserDetailService;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserDetailController {

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private ServiceControllerUtils scutils;

    @PostMapping("/register")
    public ResponseEntity<ServiceResponse> registerUser(@RequestBody UserDetailDTO userDetailDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            Map<String, Object> result = userDetailService.registerUser(userDetailDTO);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User registered successfully.");
            response.addData("user", result); // Add entire map or specific fields
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Registration failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/{userId}")
    public ResponseEntity<ServiceResponse> getUserById(@PathVariable Long userId) {
        ServiceResponse response = new ServiceResponse();
        try {
            UserDetail userDetail = userDetailService.getUserById(userId);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User fetched successfully.");
            response.addData("user", userDetail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Error fetching user: " + e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ServiceResponse> updateUser(@PathVariable Long userId, @RequestBody UserDetailDTO userDetailDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            UserDetail updatedUser = userDetailService.updateUser(userId, userDetailDTO);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User updated successfully.");
            response.addData("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Update failed: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ServiceResponse> deactivateUser(@PathVariable Long userId) {
        ServiceResponse response = new ServiceResponse();
        try {
            userDetailService.deactivateUser(userId);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User deactivated successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Deactivation failed: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ServiceResponse> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            response = userDetailService.resetPasswordByEmail(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage()));
        }
    }

} 