package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ChannelCategoryDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import java.util.List;

public interface ChannelCategoryService {
    ServiceResponse createCategory(ChannelCategoryDTO dto, Long channelId, Long companyId);
    ServiceResponse getCategoryTree(Long channelId, Long companyId);
    ServiceResponse getLeafCategories(Long channelId, Long companyId);
    ServiceResponse getCategoriesByParent(Long parentId, Long companyId);
    ServiceResponse updateCategory(Long categoryId, ChannelCategoryDTO dto, Long companyId);
    ServiceResponse deleteCategory(Long categoryId, Long companyId);
}
