package com.example.multimedia.file_upload_api.dto.sap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SapSyncResponse {
    private String status;
    private String message;
    private Integer totalRecords;
}
