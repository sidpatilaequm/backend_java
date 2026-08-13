package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class SubcategoryDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private Long parentSubcategoryId;
    private Long companyId;
    private Integer levelNo;
    private Boolean isActive;
}
