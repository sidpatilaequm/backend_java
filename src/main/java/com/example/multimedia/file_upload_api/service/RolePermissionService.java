package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.RolePermissionDTO;
import com.example.multimedia.file_upload_api.dto.RolePermissionUpdateRequest;
import com.example.multimedia.file_upload_api.dto.PermissionItemDto;
import com.example.multimedia.file_upload_api.enums.UserType;

import java.util.List;

public interface RolePermissionService {
    List<RolePermissionDTO> getPermissionsByRole(UserType role);
    void saveRolePermissions(RolePermissionUpdateRequest request);
    List<PermissionItemDto> getRolePermissionsTree(UserType role);
}

