package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChannelCategoryDTO {
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Long parentCategoryId;
    private Integer levelNo;
    private String fullPath;
    private String externalCategoryId;
    private Boolean isLeaf;
    private Integer sortOrder;
    private Boolean isActive;
    private String description;
    private Long productCount;
    private List<ChannelCategoryDTO> children;
}