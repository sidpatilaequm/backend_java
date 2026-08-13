package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ItemSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemSubcategoryRepository extends JpaRepository<ItemSubcategory, Long> {
    List<ItemSubcategory> findByItemCategory_ItemCategoryId(Long itemCategoryId);
    List<ItemSubcategory> findByIsActive(Boolean isActive);
    List<ItemSubcategory> findByItemCategory_ItemCategoryIdAndIsActive(Long itemCategoryId, Boolean isActive);
    Optional<ItemSubcategory> findByItemSubcategoryNameAndItemCategory_ItemCategoryId(String name, Long itemCategoryId);
    Optional<ItemSubcategory> findByItemSubcategoryNameIgnoreCaseAndItemCategory_ItemCategoryId(String name, Long categoryId);
    
    // Company-based filtering methods
    List<ItemSubcategory> findByCompany_CompanyId(Long companyId);
    List<ItemSubcategory> findByCompany_CompanyIdAndIsActive(Long companyId, Boolean isActive);
    List<ItemSubcategory> findByCompany_CompanyIdAndItemCategory_ItemCategoryId(Long companyId, Long itemCategoryId);
    List<ItemSubcategory> findByCompany_CompanyIdAndItemCategory_ItemCategoryIdAndIsActive(Long companyId, Long itemCategoryId, Boolean isActive);
    Optional<ItemSubcategory> findByItemSubcategoryNameAndItemCategory_ItemCategoryIdAndCompany_CompanyId(String name, Long itemCategoryId, Long companyId);
    Optional<ItemSubcategory> findByItemSubcategoryNameIgnoreCaseAndItemCategory_ItemCategoryIdAndCompany_CompanyId(String name, Long itemCategoryId, Long companyId);
    List<ItemSubcategory> findByItemCategory_ItemCategoryIdAndParentSubcategoryIsNull(Long categoryId);
    List<ItemSubcategory> findByParentSubcategory_ItemSubcategoryId(Long parentId);
    long countByItemCategory_ItemCategoryId(Long categoryId);
}
 