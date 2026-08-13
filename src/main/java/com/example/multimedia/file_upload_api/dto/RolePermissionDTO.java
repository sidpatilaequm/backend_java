package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class RolePermissionDTO {
    private Long permissionId;
    private String permissionName;
    private Boolean canCreate;
    private Boolean canView;
    private Boolean canEdit;
    private Boolean canDelete;
}
