package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class CardDataDTO {
    private String companyName;
    private String cardHolderName;
    private String designation;
    private List<String> mobileNumbers;
    private List<String> emailAddresses;
    private String websiteUrl;
    private String area;
    private String city;
    private String state;
    private String pincode;
} 