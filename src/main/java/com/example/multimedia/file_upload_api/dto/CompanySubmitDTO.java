package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class CompanySubmitDTO {
    private String reg;
    private String businessName;
    private String rocCode;
    private String registrationNumber;
    private String category;
    private String subCategory;
    private String companyClass;
    private String authorizedCapital;
    private String paidCapital;
    private String incorporatedDate;
    private String email;
    private Boolean listed;
    private String lastAGMDate;
    private String lastBSDate;
    private Boolean active;
    private String status;
    private String addressesJson;
    private String directorsJson;
    private String chargesJson;
    private String efilingsJson;
    private Long companyId; // Optional admin override
}
