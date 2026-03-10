package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.MaterialVariant;
import com.example.multimedia.file_upload_api.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MaterialVariantRepository extends JpaRepository<MaterialVariant, Long> {
    boolean existsByVariantCode(String variantCode);
    Optional<MaterialVariant> findByVariantCode(String variantCode);
    int countByMaterial(Material material);
    List<MaterialVariant> findAll();
    List<MaterialVariant> findByMaterial(Material material);
    
    Optional<MaterialVariant> findByMaterialAndVariantCode(Material material, String variantCode);
    
    // New methods for admin filtering
    List<MaterialVariant> findByMaterial_SuperAdmin_SuperAdminId(Long superAdminId);
} 