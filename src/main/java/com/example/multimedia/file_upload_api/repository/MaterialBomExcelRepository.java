package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.MaterialBomExcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaterialBomExcelRepository extends JpaRepository<MaterialBomExcel, Long> {
    Optional<MaterialBomExcel> findByMaterial_MaterialIdAndSuperAdmin_SuperAdminId(Long materialId, Long superAdminId);
    boolean existsByMaterial_MaterialIdAndSuperAdmin_SuperAdminId(Long materialId, Long superAdminId);
}

