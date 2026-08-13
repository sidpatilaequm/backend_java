package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.VendorPermissionRequestDto;
import com.example.multimedia.file_upload_api.dto.VendorPermissionResponseDto;

public interface VendorPermissionService {
    void saveVendorPermissions(VendorPermissionRequestDto requestDto);
    VendorPermissionResponseDto getVendorPermissions(Long companyId);
    VendorPermissionResponseDto getMyPermissions();
    VendorPermissionResponseDto getPermissionsForLogin(Long companyId);
    boolean hasPermission(Long companyId, String permissionCode, String action);
}
