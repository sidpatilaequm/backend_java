package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class PanSubmitDTO {
    private String panNumber;
    private String name;
    private String dateOfBirthIncorporation;
    private String category;
    private String fathersName;
    private Long companyId; // Optional admin override
}
