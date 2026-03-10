package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MaterialChannelMappingRequest {
    private Long materialId;
    private List<ChannelMappingRequest> mappings;
    
    @Data
    public static class ChannelMappingRequest {
        private Long channelId;
        private Long categoryId;
        private BigDecimal price;
        private Integer stock;
        private Boolean status = true;
    }
}
