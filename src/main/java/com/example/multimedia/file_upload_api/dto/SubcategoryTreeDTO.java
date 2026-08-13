package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class SubcategoryTreeDTO {
    private Long id;
    private String name;
    private Integer levelNo;
    private List<SubcategoryTreeDTO> children = new ArrayList<>();
}
