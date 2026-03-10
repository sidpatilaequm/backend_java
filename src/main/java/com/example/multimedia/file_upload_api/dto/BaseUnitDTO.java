package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseUnitDTO {
    private String code;
    private String description;
    private String displayValue;
} 