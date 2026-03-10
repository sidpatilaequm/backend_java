package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class LocationDTO {
    private Long locationId;
    private String locationName;
    private String pinCode;
    private String address;
    private String city;
    private String state;
    private String country;
    private Boolean isActive;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;
}
