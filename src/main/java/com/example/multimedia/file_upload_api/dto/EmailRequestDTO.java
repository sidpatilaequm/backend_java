package com.example.multimedia.file_upload_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequestDTO {
    private Long userId;
    private String subject;
    private String body;

    // Getters and setters
}
