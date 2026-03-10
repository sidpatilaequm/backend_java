package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Attribute;
import com.example.multimedia.file_upload_api.entity.AttributeType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Optional<Attribute> findByAttributeName(String attributeName);
    List<Attribute> findByIsActive(Boolean isActive);
    boolean existsByAttributeName(String attributeName);
    List<Attribute> findAllByType(AttributeType type);
    
    // New methods for data isolation
    List<Attribute> findBySuperAdmin_SuperAdminId(Long superAdminId);
    List<Attribute> findBySuperAdmin_SuperAdminIdAndIsActive(Long superAdminId, Boolean isActive);
    List<Attribute> findBySuperAdmin_SuperAdminIdAndType(Long superAdminId, AttributeType type);
    Optional<Attribute> findByAttributeNameAndSuperAdmin_SuperAdminId(String attributeName, Long superAdminId);
    boolean existsByAttributeNameAndSuperAdmin_SuperAdminId(String attributeName, Long superAdminId);
} 