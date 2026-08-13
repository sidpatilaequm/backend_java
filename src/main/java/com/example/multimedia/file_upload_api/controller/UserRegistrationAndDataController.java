package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.UserRegistrationAndDataDTO;
import com.example.multimedia.file_upload_api.dto.RoleUserRegistrationRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.UserRegistrationAndDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
public class UserRegistrationAndDataController {

    @Autowired
    private UserRegistrationAndDataService userRegistrationAndDataService;

    @PostMapping("/register-role-user")
    public ResponseEntity<ServiceResponse> registerRoleUser(@RequestBody RoleUserRegistrationRequest registrationDTO) {
        ServiceResponse response = userRegistrationAndDataService.registerRoleUser(registrationDTO);
        
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete")
    public ResponseEntity<ServiceResponse> registerUserAndSaveData(@RequestBody UserRegistrationAndDataDTO registrationDTO) {
        ServiceResponse response = userRegistrationAndDataService.processUserRegistrationAndData(registrationDTO);
        
        // Return 400 Bad Request for error responses
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/complete")
    public ResponseEntity<ServiceResponse> getCompleteVendorDetails(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long userId) {
        
        ServiceResponse response = userRegistrationAndDataService.getCompleteVendorDetails(companyId, userId);
        
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
} 