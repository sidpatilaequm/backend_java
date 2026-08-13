package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CategoryChannelMapping;
import com.example.multimedia.file_upload_api.entity.Channel;
import com.example.multimedia.file_upload_api.entity.ChannelCategory;
import com.example.multimedia.file_upload_api.entity.ItemCategory;
import com.example.multimedia.file_upload_api.repository.CategoryChannelMappingRepository;
import com.example.multimedia.file_upload_api.repository.ChannelCategoryRepository;
import com.example.multimedia.file_upload_api.repository.ChannelRepository;
import com.example.multimedia.file_upload_api.repository.ItemCategoryRepository;
import com.example.multimedia.file_upload_api.service.CategoryMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryMappingServiceImpl implements CategoryMappingService {

    private final CategoryChannelMappingRepository mappingRepository;
    private final ItemCategoryRepository internalCategoryRepository;
    private final ChannelCategoryRepository channelCategoryRepository;
    private final ChannelRepository channelRepository;

    @Override
    @Transactional
    public ServiceResponse mapCategories(Long internalCategoryId, Long channelCategoryId, Long channelId, Long companyId) {
        ItemCategory internal = internalCategoryRepository.findById(internalCategoryId)
                .orElseThrow(() -> new RuntimeException("Internal category not found"));
        ChannelCategory external = channelCategoryRepository.findById(channelCategoryId)
                .orElseThrow(() -> new RuntimeException("Channel category not found"));
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        if (!channel.getCompany().getCompanyId().equals(companyId)) {
            return ServiceResponse.builder().status(false).message("Unauthorized").build();
        }

        CategoryChannelMapping mapping = mappingRepository.findByInternalCategory_ItemCategoryIdAndChannel_ChannelIdAndChannel_Company_CompanyId(internalCategoryId, channelId, companyId)
                .orElse(new CategoryChannelMapping());

        mapping.setInternalCategory(internal);
        mapping.setChannelCategory(external);
        mapping.setChannel(channel);

        mappingRepository.save(mapping);
        return ServiceResponse.builder()
                .status(true)
                .message("Category mapped successfully")
                .build();
    }

    @Override
    public ServiceResponse getMappingsByChannel(Long channelId, Long companyId) {
        return ServiceResponse.builder()
                .status(true)
                .data(mappingRepository.findByChannel_ChannelIdAndChannel_Company_CompanyId(channelId, companyId))
                .build();
    }
}
