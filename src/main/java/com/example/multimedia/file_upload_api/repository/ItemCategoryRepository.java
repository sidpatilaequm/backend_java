package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    Optional<ItemCategory> findByCode(String code);
    Optional<ItemCategory> findByCodeIgnoreCase(String code);
    List<ItemCategory> findByIsActive(Boolean isActive);
    boolean existsByCode(String code);
    List<ItemCategory> findByCompany_CompanyId(Long companyId);
    boolean existsByCategoryNameAndCompany_CompanyId(String categoryName, Long companyId);
} 