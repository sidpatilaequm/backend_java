package com.example.multimedia.file_upload_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AsnPackageDto {

    @JsonProperty("package_number")
    private Integer packageNumber;

    @JsonProperty("material_details")
    private String materialDetails;

    @JsonProperty("quantity")
    private Double quantity;
}
