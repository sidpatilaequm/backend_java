package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.enums.UserType;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionUpdateRequest {
    private UserType role;
    private List<RolePermissionDTO> permissions;
}
