package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;

@Data
public class AuditLogDto {
    private String time;
    private String message;
    private String kind;
}
