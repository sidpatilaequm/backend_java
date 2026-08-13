package com.example.multimedia.file_upload_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AsnItemRequestDto {

    @JsonProperty("line_number")
    private Integer lineNumber;

    @JsonProperty("part_number")
    private String partNumber;

    @JsonProperty("quantity_shipped")
    private BigDecimal quantityShipped;

    @JsonProperty("batch_heat_number")
    private String batchHeatNumber;
}
