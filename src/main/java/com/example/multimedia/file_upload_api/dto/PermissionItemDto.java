package com.example.multimedia.file_upload_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionItemDto {
    private String permissionCode;
    private String permissionName;
    private String permissionType; // MODULE, SUB_MODULE, BLOCK
    private Boolean view;
    private Boolean create;
    private Boolean edit;
    private Boolean delete;
    private java.util.List<PermissionItemDto> children;
}
