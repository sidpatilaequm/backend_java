package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class MsmeSubmitDTO {
    private String udyamNumber;
    private String entityName;
    private String type;
    private String majorActivity;
    private String gender;
    private String socialCategory;
    private String incorporatedDate;
    private String commencedDate;
    private String registeredDate;
    private Long companyId; // Optional admin override
}
