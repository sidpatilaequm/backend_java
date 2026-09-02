package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Inventory;
import com.example.multimedia.file_upload_api.entity.Material;

import com.example.multimedia.file_upload_api.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Find by material and location
    Optional<Inventory> findByMaterialAndLocationAndSuperAdmin_SuperAdminIdAndIsActiveTrue(
        Material material, Location location, Long superAdminId);

    // Find all inventory for a location and super admin
    List<Inventory> findByLocationAndSuperAdmin_SuperAdminIdAndIsActiveTrue(Location location, Long superAdminId);

    // Find all inventory for a material
    List<Inventory> findByMaterialAndSuperAdmin_SuperAdminIdAndIsActiveTrue(Material material, Long superAdminId);
    
    // Find all inventory for a material (without filters, for deletion purposes)
    List<Inventory> findByMaterial(Material material);

    // Find by SKU and location
    @Query("SELECT i FROM Inventory i WHERE i.material.sku = :sku AND i.location.locationId = :locationId AND i.superAdmin.superAdminId = :superAdminId AND i.isActive = true")
    Optional<Inventory> findBySkuAndLocationIdAndSuperAdminId(@Param("sku") String sku, @Param("locationId") Long locationId, @Param("superAdminId") Long superAdminId);

    // Find all inventory by SKU and location
    @Query("SELECT i FROM Inventory i WHERE i.material.sku = :sku AND i.location.locationId = :locationId AND i.superAdmin.superAdminId = :superAdminId AND i.isActive = true")
    List<Inventory> findAllBySkuAndLocationIdAndSuperAdminId(@Param("sku") String sku, @Param("locationId") Long locationId, @Param("superAdminId") Long superAdminId);

    // Check if inventory exists for material and location
    boolean existsByMaterialAndLocationAndSuperAdmin_SuperAdminIdAndIsActiveTrue(
        Material material, Location location, Long superAdminId);
}
