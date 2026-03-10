package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // Find cart items by company
    List<Cart> findByCompany_CompanyId(Long companyId);
    
    // Find cart items by company and channel
    List<Cart> findByCompany_CompanyIdAndChannel_ChannelId(Long companyId, Long channelId);
    
    // Find specific cart item by company, material, and channel (for duplicate check)
    Optional<Cart> findByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
        Long companyId, Long materialId, Long channelId);
    
    // Check if item exists in cart for company
    boolean existsByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
        Long companyId, Long materialId, Long channelId);
    
    // Delete cart items by company
    void deleteByCompany_CompanyId(Long companyId);
    
    // Delete cart items by company and channel
    void deleteByCompany_CompanyIdAndChannel_ChannelId(Long companyId, Long channelId);
    
    // Get cart summary by company
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.company.companyId = :companyId")
    Integer countItemsByCompanyId(@Param("companyId") Long companyId);
    
    // Get total price by company
    @Query("SELECT COALESCE(SUM(c.totalPrice), 0) FROM Cart c WHERE c.company.companyId = :companyId")
    Double getTotalPriceByCompanyId(@Param("companyId") Long companyId);
    
    // Get cart items with channel and company info
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.channel " +
           "LEFT JOIN FETCH c.company " +
           "LEFT JOIN FETCH c.material " +
           "WHERE c.company.companyId = :companyId " +
           "ORDER BY c.addedAt DESC")
    List<Cart> findCartItemsWithDetailsByCompanyId(@Param("companyId") Long companyId);
    
    // Get cart items with optional filters
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.channel " +
           "LEFT JOIN FETCH c.company " +
           "LEFT JOIN FETCH c.material " +
           "WHERE c.company.companyId = :companyId " +
           "AND (:channelId IS NULL OR c.channel.channelId = :channelId) " +
           "ORDER BY c.addedAt DESC")
    List<Cart> findCartItemsWithFilters(@Param("companyId") Long companyId, 
                                       @Param("channelId") Long channelId);
}
