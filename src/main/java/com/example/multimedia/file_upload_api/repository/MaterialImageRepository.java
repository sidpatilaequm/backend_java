package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.MaterialImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialImageRepository extends JpaRepository<MaterialImage, Long> {
    List<MaterialImage> findByMaterialOrderBySequenceOrderAsc(Material material);

    List<MaterialImage> findByMaterial_MaterialId(Long materialId);

    void deleteByMaterialAndImageId(Material material, Long imageId);

    Integer countByMaterial(Material material);
}