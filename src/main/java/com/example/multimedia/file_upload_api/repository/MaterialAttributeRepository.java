package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.MaterialAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialAttributeRepository extends JpaRepository<MaterialAttribute, Long> {
    List<MaterialAttribute> findByMaterial_MaterialId(Long materialId);
    List<MaterialAttribute> findByVariant_Id(Long variantId);
    List<MaterialAttribute> findByAttribute_AttributeId(Long attributeId);
    boolean existsByAttribute_AttributeId(Long attributeId);
} 