package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.MaterialChannelListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialChannelListingRepository extends JpaRepository<MaterialChannelListing, Long> {
    List<MaterialChannelListing> findByCompany_CompanyId(Long companyId);
    List<MaterialChannelListing> findByMaterial_MaterialIdAndCompany_CompanyId(Long materialId, Long companyId);
    List<MaterialChannelListing> findByChannel_ChannelIdAndCompany_CompanyId(Long channelId, Long companyId);
    Optional<MaterialChannelListing> findByMaterial_MaterialIdAndChannel_ChannelIdAndCompany_CompanyId(Long materialId, Long channelId, Long companyId);
    void deleteByChannel_ChannelId(Long channelId);
    void deleteByChannelCategory_CategoryId(Long categoryId);
}
