package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class GstSubmitDTO {
    private String gstinNumber;
    private String legalTradeName;
    private String companyName;
    private String registeredAddress;
    private String typeOfRegistration;
    private String dateOfRegistration; // yyyy-MM-dd
    private Long companyId; // Optional admin override
}
