package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

/** Payload for POST .../draft — matches SupplierRegistration's own field shape 1:1. */
@Data
public class SupplierDraftDTO {
    private Long registrationId;
    private String resumeCode;

    private String vendorName;
    private String address;

    private String contact1Name;
    private String contact1Role;
    private String contact1Email;
    private String contact1Phone;

    private String contact2Name;
    private String contact2Role;
    private String contact2Email;
    private String contact2Phone;

    private Integer primaryContact;

    /** Comma-separated on the wire, same as how it's stored. */
    private String supplyCategories;
    private String plant;
    private String paymentTerms;
    private Boolean declarationAccepted;

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
