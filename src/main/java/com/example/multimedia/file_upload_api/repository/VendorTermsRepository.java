package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.VendorTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorTermsRepository extends JpaRepository<VendorTerms, Long> {
    List<VendorTerms> findByUser_UserId(Long userId);
    List<VendorTerms> findByCompany_CompanyId(Long companyId);
    List<VendorTerms> findByIsActive(Boolean isActive);
} 