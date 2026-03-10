package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class ExtractedDataDTO {
    private GstDataDTO gst;
    private PanDataDTO pan;
    private ChequeDataDTO cheque;
    private String cinNo; // Optional, for Certificate of Incorporation
}

@Data
class GstDataDTO {
    private String gstin;
    private String legalName;
    private String tradeName;
    private String constitution;
    private String address;
    private String type;
    private String registered;
    private String issued;
    private String approvingAuthority;
    private String jurisdiction;
    private boolean provisional;
    private String contactPerson;
    private String phoneNumber;
}

@Data
class PanDataDTO {
    private String pan;
    private String incorporationDate;
    private String name;
    private String category;
    private String address;
    private String fatherName;
    private String dateOfIssue;
    private String contactPerson;
    private String phoneNumber;
}

@Data
class ChequeDataDTO {
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String branch;
    private String issuedTo;
    private String signatory;
    private String code;
    private String date;
    private String amount;
    private String contactPerson;
    private String phoneNumber;
} 