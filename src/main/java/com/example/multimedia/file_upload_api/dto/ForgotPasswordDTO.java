package com.example.multimedia.file_upload_api.dto;


import lombok.Data;

@Data
public class ForgotPasswordDTO {
    private String email;
    private String newPassword;
}
