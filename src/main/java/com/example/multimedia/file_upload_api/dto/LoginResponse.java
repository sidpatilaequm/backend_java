package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String email;
    private Long userId;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private Integer authId;
    private String authName;
    private Object permissions; // Hierarchical tree of permissions
    private Boolean isDocumentsPresent;

    public LoginResponse(String token, String email, Long userId, String firstname, String lastname, String phoneNumber) {
        this.token = token;
        this.email = email;
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
    }

    public void setAuthId(Integer authId) {
        this.authId = authId;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }
} 