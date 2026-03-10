package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;
 
@Data
public class VariantBulkRequest {
    private List<VariantRequest> variants;
} 