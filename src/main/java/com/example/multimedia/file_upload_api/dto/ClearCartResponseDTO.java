package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClearCartResponseDTO {
    
    private Integer clearedItems;
    private List<String> clearedChannels;
    private List<String> clearedCompanies;
}
