package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChannelCreateRequest {
    private String channelName;
    private String channelCode;
    private String description;
    private List<ChannelCategoryRequest> categories;
    
    @Data
    public static class ChannelCategoryRequest {
        private String categoryCode;
        private String categoryName;
    }
}
