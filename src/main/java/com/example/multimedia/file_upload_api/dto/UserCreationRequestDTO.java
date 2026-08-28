package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class UserCreationRequestDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role; // e.g. "EMPLOYEE", "ADMIN", "PURCHASE_DEPT"
    private String deptCode; // Optional
    private String companyCode;
    private String plantCode;
    private String purchOrgCode;
}
