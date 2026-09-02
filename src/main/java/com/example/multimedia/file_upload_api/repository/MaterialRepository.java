package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findBySku(String sku);

    List<Material> findByBlocked(Boolean blocked);
    boolean existsBySku(String sku);
    boolean existsBySkuAndSuperAdmin_SuperAdminId(String sku, Long superAdminId);
    boolean existsBySkuAndSuperAdmin_SuperAdminIdAndLocation_LocationId(String sku, Long superAdminId, Long locationId);
    boolean existsByMaterialCode(String materialCode);
    Optional<Material> findByMaterialCode(String materialCode);
    
    // New methods for admin filtering
    List<Material> findBySuperAdmin_SuperAdminId(Long superAdminId);

    List<Material> findBySuperAdmin_SuperAdminIdAndBlocked(Long superAdminId, Boolean blocked);
    List<Material> findBySuperAdmin_SuperAdminIdAndLocation_LocationId(Long superAdminId, Long locationId);
    
    // Vendor specific
    List<Material> findByVendorId(Long vendorId);
    
    @org.springframework.data.jpa.repository.Query("SELECT m FROM Material m WHERE m.superAdmin.superAdminId = :superAdminId " +
            "AND (:locationId IS NULL OR m.location.locationId = :locationId)")
    List<Material> filterMaterials(
            @org.springframework.data.repository.query.Param("superAdminId") Long superAdminId,
            @org.springframework.data.repository.query.Param("locationId") Long locationId
    );

    // Deletion check methods

    
    // Find material by ID and super admin ID (to avoid lazy loading issues)
    Optional<Material> findByMaterialIdAndSuperAdmin_SuperAdminId(Long materialId, Long superAdminId);
} 