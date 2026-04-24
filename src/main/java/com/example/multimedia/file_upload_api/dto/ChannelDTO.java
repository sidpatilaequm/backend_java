package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChannelDTO {
    private Long channelId;
    private String channelName;
    private String channelCode;
    private String description;
    private Boolean isActive;
    private Long companyId;
    private Long userId;
    private String status;
    private CountryDTO country;
    private CurrencyDTO currency;
    private List<ChannelCategoryDTO> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
