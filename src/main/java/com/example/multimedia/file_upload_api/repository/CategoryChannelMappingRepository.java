package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.CategoryChannelMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryChannelMappingRepository extends JpaRepository<CategoryChannelMapping, Long> {
    List<CategoryChannelMapping> findByChannel_ChannelIdAndChannel_Company_CompanyId(Long channelId, Long companyId);
    Optional<CategoryChannelMapping> findByInternalCategory_ItemCategoryIdAndChannel_ChannelIdAndChannel_Company_CompanyId(Long internalCategoryId, Long channelId, Long companyId);
    void deleteByChannel_ChannelId(Long channelId);
    void deleteByChannelCategory_CategoryId(Long categoryId);
}
