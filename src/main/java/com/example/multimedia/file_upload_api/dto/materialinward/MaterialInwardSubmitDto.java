package com.example.multimedia.file_upload_api.dto.materialinward;

import lombok.Data;

@Data
public class MaterialInwardSubmitDto {
    private String decision; // ACCEPT, REJECT
    private String by;
    private String reason;
    private String inwardDetailsJson; // Store the exact verification details
}
