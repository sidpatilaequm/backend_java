package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;

public interface CategoryMappingService {
    ServiceResponse mapCategories(Long internalCategoryId, Long channelCategoryId, Long channelId, Long companyId);
    ServiceResponse getMappingsByChannel(Long channelId, Long companyId);
}
