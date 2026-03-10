package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.MaterialChannelMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialChannelMappingRepository extends JpaRepository<MaterialChannelMapping, Long> {
    
    /**
     * Find mapping by company, material, and channel (for upsert operations)
     */
    Optional<MaterialChannelMapping> findByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
            Long companyId, Long materialId, Long channelId);
    
    /**
     * Find all mappings for a specific material within a company
     */
    List<MaterialChannelMapping> findByCompany_CompanyIdAndMaterial_MaterialId(
            Long companyId, Long materialId);
    
    /**
     * Find all mappings for a specific channel within a company
     */
    List<MaterialChannelMapping> findByCompany_CompanyIdAndChannel_ChannelId(
            Long companyId, Long channelId);
    
    /**
     * Find all mappings for a company
     */
    List<MaterialChannelMapping> findByCompany_CompanyId(Long companyId);
    
    /**
     * Check if mapping exists for company, material, and channel
     */
    boolean existsByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
            Long companyId, Long materialId, Long channelId);
    
    /**
     * Delete mapping by company, material, and channel
     */
    void deleteByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
            Long companyId, Long materialId, Long channelId);
    
    /**
     * Find mappings by material ID and company ID with channel and category details
     */
    @Query("SELECT mcm FROM MaterialChannelMapping mcm " +
           "JOIN FETCH mcm.channel c " +
           "LEFT JOIN FETCH mcm.category cat " +
           "WHERE mcm.material.materialId = :materialId " +
           "AND mcm.company.companyId = :companyId")
    List<MaterialChannelMapping> findByMaterialIdAndCompanyIdWithDetails(
            @Param("materialId") Long materialId, 
            @Param("companyId") Long companyId);

    /**
     * Count mappings by channel and category
     */
    @Query("SELECT COUNT(mcm) FROM MaterialChannelMapping mcm " +
           "WHERE mcm.channel.channelId = :channelId " +
           "AND mcm.category.categoryId = :categoryId " +
           "AND mcm.status = :status")
    Long countByChannel_ChannelIdAndCategory_CategoryIdAndStatus(
            @Param("channelId") Long channelId, 
            @Param("categoryId") Long categoryId, 
            @Param("status") Boolean status);
}
