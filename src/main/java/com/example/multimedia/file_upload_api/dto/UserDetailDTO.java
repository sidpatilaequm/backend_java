package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDetailDTO {
    private Long userId;
    private Long superAdminId;
    private String email;
    private String password;
    private String signupDate;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String authKey;
} 