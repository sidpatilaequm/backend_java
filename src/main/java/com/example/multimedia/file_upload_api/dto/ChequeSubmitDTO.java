package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class ChequeSubmitDTO {
    private String accountNumber;
    private String ifscCode;
    private String bankName;
    private String branch;
    private String signatory;
    private String issuedTo;
    private String issued;
    private String code;
    private Long companyId; // Optional admin override
}
