package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCategoryDTO {
    
    private Long categoryId;
    private String categoryName;
    private String categoryCode;
    private String description;
    private Boolean isActive;
    private Long productCount; // Number of products in this category for this channel
}