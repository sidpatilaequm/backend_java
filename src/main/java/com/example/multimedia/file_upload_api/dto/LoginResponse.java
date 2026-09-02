package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String email;
    private Long userId;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private Integer authId;
    private String authName;
    private Object permissions; // Hierarchical tree of permissions
    private Boolean isDocumentsPresent;
    private String redirectUrl;
    // The real VendorMaster.vendor_id — deliberately separate from permissions.vendorId, which is
    // actually CompanyDetails.company_id (correct for permission-scoping, but a different ID
    // space). PR/RFQ/ASN/quotation assignment all key off this one; conflating the two silently
    // broke vendor-side visibility for any account where the two IDs differ, which is effectively
    // always since they're separate auto-increment sequences.
    private Long vendorMasterId;

    public LoginResponse(String token, String email, Long userId, String firstname, String lastname, String phoneNumber) {
        this.token = token;
        this.email = email;
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
    }

    public void setAuthId(Integer authId) {
        this.authId = authId;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }
} 