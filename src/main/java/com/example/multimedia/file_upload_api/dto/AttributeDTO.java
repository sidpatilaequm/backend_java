package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.example.multimedia.file_upload_api.entity.AttributeType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDTO {
    private Long attributeId;
    private String attributeName;
    private Boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private AttributeType type;
} 