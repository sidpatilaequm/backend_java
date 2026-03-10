package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationAndDataDTO {
    // User Registration Fields
    @Deprecated // No longer needed - super admin is obtained from security context
    private Long superAdminId;
    private String email;
    private String password;  // Password provided by user
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String authKey;

    // Company Details
    private String gstinNumber;
    private String legalTradeName;
    private String panNumber;
    private String panTinCst;
    private String dateOfRegistration;
    private String typeOfRegistration;
    private String registeredAddress;

    // File names
    private String gstFileName;
    private String panFileName;
    private String chequeFileName;
    private String coiFileName;

    // Bank Details
    private String accountNumber;
    private String ifsc;
    private String branch;
    private String bank;
    private String code;

    // PAN Details
    private String name;
    private String dateOfBirthIncorporation;

    // COI Details
    private String cinNumber;
} 