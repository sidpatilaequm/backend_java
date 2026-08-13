package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;

@Data
public class SupervisorReleaseDto {
    private String action; // APPROVE_WITH_SHORTAGE, REJECT
    private String supervisorRemark;
}
