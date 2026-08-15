package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

/** Payload for POST .../draft — matches SupplierRegistration's own field shape 1:1. */
@Data
public class SupplierDraftDTO {
    private Long registrationId;
    private String resumeCode;

    private String vendorName;
    private String address;
    private String contactName;
    private String designation;
    private String email;
    private String phone;

    private String gstNumber;
    private String panNumber;
    private String msmeNumber;
    private String cinNumber;

    private String beneficiaryName;
    private String accountNumber;
    private String ifscCode;
    private String bankName;

    private String isoCertificateNo;
    private String isoCertifyingBody;
    private String isoExpiry;

    private String as9100dCertificateNo;
    private String as9100dCertifyingBody;
    private String as9100dExpiry;
}
