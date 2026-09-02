package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemainingProductDTO {
    
    private Long materialId;
    private String materialName;
    private String materialCode;
    private String materialDescription;
    private BigDecimal price;
    private BigDecimal channelSpecificPrice;
}
