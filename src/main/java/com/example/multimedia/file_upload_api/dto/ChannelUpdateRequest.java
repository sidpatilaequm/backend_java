package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChannelUpdateRequest {
    private String channelName;
    private String channelCode;
    private String description;
    private Boolean isActive;
    private List<ChannelCategoryRequest> categories;
    
    @Data
    public static class ChannelCategoryRequest {
        private Long categoryId;
        private String categoryCode;
        private String categoryName;
        private Boolean isActive;
    }
}
