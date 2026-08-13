package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class VendorAddressDto {
    private String streetAndHouseNumber;
    private String streetName1;
    private String cityName;
    private String postalCode;
    private String countryCode;
}
