package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class VendorPermissionRequestDto {
    private Long vendorId; // This is companyId
    private List<PermissionItemDto> permissions;
}
