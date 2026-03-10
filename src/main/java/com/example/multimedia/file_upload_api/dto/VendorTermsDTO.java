package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class VendorTermsDTO {
    private Long vendorTermsId;
    private Long userId;
    private Long companyId;
    private Boolean isActive;
    
    // File data
    private MultipartFile paymentTermsFile;
    private String paymentTermsFileName;
    private String paymentTermsFileType;
    
    private MultipartFile incotermsFile;
    private String incotermsFileName;
    private String incotermsFileType;
    
    private MultipartFile deliveryTermsFile;
    private String deliveryTermsFileName;
    private String deliveryTermsFileType;
    
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
} 