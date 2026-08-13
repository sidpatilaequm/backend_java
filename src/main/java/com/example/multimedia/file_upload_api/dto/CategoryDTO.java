package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String categoryName;
    private String code;
    private Long companyId;
    private Boolean isActive;
}
