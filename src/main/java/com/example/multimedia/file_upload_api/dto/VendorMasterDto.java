package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class VendorMasterDto {
    private String bpNo;
    private String vendorName;
    private VendorAddressDto vendorAddress;
    private String emailAddress;
    private String gstNumber;
    private String bankAccountNumber;
}
