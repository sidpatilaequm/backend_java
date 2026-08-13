package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AsnItemResponseDto {
    private Long id;
    private Integer lineNumber;
    private String partNumber;
    private BigDecimal quantityShipped;
    private String batchHeatNumber;
    private String testCertUrl;
}
