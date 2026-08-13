package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class RoleUserRegistrationRequest {
    private String email;
    private String name;
    private String phoneNumber;
    private String authKey; // e.g. "procurement_manager", "employee", "vendor", "administrator"
}
