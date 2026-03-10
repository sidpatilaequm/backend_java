package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDTO {
    
    private Integer totalItems;
    private BigDecimal totalPrice;
    private Integer totalChannels;
    private List<ChannelSummaryDTO> channels;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelSummaryDTO {
        private Long channelId;
        private String channelCode;
        private String channelName;
        private Integer itemCount;
        private BigDecimal channelTotal;
    }
}
