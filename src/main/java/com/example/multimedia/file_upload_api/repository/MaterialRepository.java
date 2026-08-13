package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findBySku(String sku);
    List<Material> findByItemCategory_ItemCategoryId(Long categoryId);
    List<Material> findBySubcategory_ItemSubcategoryId(Long subcategoryId);
    List<Material> findByBlocked(Boolean blocked);
    boolean existsBySku(String sku);
    boolean existsBySkuAndSuperAdmin_SuperAdminId(String sku, Long superAdminId);
    boolean existsBySkuAndSuperAdmin_SuperAdminIdAndLocation_LocationId(String sku, Long superAdminId, Long locationId);
    boolean existsByMaterialCode(String materialCode);
    Optional<Material> findByMaterialCode(String materialCode);
    
    // New methods for admin filtering
    List<Material> findBySuperAdmin_SuperAdminId(Long superAdminId);
    List<Material> findBySuperAdmin_SuperAdminIdAndItemCategory_ItemCategoryId(Long superAdminId, Long categoryId);
    List<Material> findBySuperAdmin_SuperAdminIdAndSubcategory_ItemSubcategoryId(Long superAdminId, Long subcategoryId);
    List<Material> findBySuperAdmin_SuperAdminIdAndBlocked(Long superAdminId, Boolean blocked);
    List<Material> findBySuperAdmin_SuperAdminIdAndLocation_LocationId(Long superAdminId, Long locationId);
    
    // Vendor specific
    List<Material> findByVendorId(Long vendorId);
    
    @org.springframework.data.jpa.repository.Query("SELECT m FROM Material m WHERE m.superAdmin.superAdminId = :superAdminId " +
            "AND (:locationId IS NULL OR m.location.locationId = :locationId) " +
            "AND (:categoryId IS NULL OR m.itemCategory.itemCategoryId = :categoryId) " +
            "AND (:l1Id IS NULL OR m.subcategoryL1.itemSubcategoryId = :l1Id) " +
            "AND (:l2Id IS NULL OR m.subcategoryL2.itemSubcategoryId = :l2Id) " +
            "AND (:l3Id IS NULL OR m.subcategoryL3.itemSubcategoryId = :l3Id)")
    List<Material> filterMaterials(
            @org.springframework.data.repository.query.Param("superAdminId") Long superAdminId,
            @org.springframework.data.repository.query.Param("locationId") Long locationId,
            @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
            @org.springframework.data.repository.query.Param("l1Id") Long l1Id,
            @org.springframework.data.repository.query.Param("l2Id") Long l2Id,
            @org.springframework.data.repository.query.Param("l3Id") Long l3Id
    );

    // Deletion check methods
    boolean existsByItemCategory_ItemCategoryId(Long categoryId);
    boolean existsBySubcategoryL1_ItemSubcategoryId(Long subcategoryId);
    boolean existsBySubcategoryL2_ItemSubcategoryId(Long subcategoryId);
    boolean existsBySubcategoryL3_ItemSubcategoryId(Long subcategoryId);
    boolean existsBySubcategory_ItemSubcategoryId(Long subcategoryId);
    
    // Find material by ID and super admin ID (to avoid lazy loading issues)
    Optional<Material> findByMaterialIdAndSuperAdmin_SuperAdminId(Long materialId, Long superAdminId);
} 