package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Boolean active;
    private List<String> roles;
    private String password;
}
