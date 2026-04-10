package com.example.multimedia.file_upload_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterBomResponseDto {

    private String status;
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        @JsonProperty("header_data")
        private HeaderData headerData;

        private List<BomItem> bom;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeaderData {
        @JsonProperty("part_number")
        private String partNumber;

        private String description;
        private String source;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BomItem {
        @JsonProperty("item_code")
        private String itemCode;

        private String description;
        private Double qty;
        private String uom;
        private Integer level;
        
        @JsonProperty("procurement_type")
        private String procurementType;
    }
}
