package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class SubcategoryBulkCreateRequest {
    private Long categoryId;
    private String name;
    private List<SubcategoryBulkCreateRequest> children = new ArrayList<>();
}
