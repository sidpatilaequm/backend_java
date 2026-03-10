package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VendorTermsResponseDTO {
    private Long vendorTermsId;
    private Long userId;
    private Long companyId;
    private Boolean isActive;
    
    private String paymentTermsFileName;
    private String paymentTermsFileType;
    
    private String incotermsFileName;
    private String incotermsFileType;
    
    private String deliveryTermsFileName;
    private String deliveryTermsFileType;
    
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
} 