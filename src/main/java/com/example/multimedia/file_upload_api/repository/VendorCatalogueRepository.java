package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.VendorCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorCatalogueRepository extends JpaRepository<VendorCatalogue, Long> {

    /**
     * Find catalogue by vendor ID
     */
    Optional<VendorCatalogue> findByVendorIdAndIsActiveTrue(Long vendorId);

    /**
     * Find all catalogues for a vendor
     */
    List<VendorCatalogue> findByVendorIdOrderByUploadDateDesc(Long vendorId);

    /**
     * Find catalogue by vendor ID and super admin
     */
    @Query("SELECT vc FROM VendorCatalogue vc WHERE vc.vendorId = :vendorId AND vc.superAdmin.superAdminId = :superAdminId AND vc.isActive = true")
    Optional<VendorCatalogue> findByVendorIdAndSuperAdminIdAndIsActiveTrue(@Param("vendorId") Long vendorId, @Param("superAdminId") Long superAdminId);

    /**
     * Check if catalogue exists for vendor
     */
    boolean existsByVendorIdAndIsActiveTrue(Long vendorId);

    /**
     * Find catalogue by vendor ID and super admin (for checking existence)
     */
    @Query("SELECT CASE WHEN COUNT(vc) > 0 THEN true ELSE false END FROM VendorCatalogue vc WHERE vc.vendorId = :vendorId AND vc.superAdmin.superAdminId = :superAdminId AND vc.isActive = true")
    boolean existsByVendorIdAndSuperAdminIdAndIsActiveTrueQuery(@Param("vendorId") Long vendorId, @Param("superAdminId") Long superAdminId);
}
