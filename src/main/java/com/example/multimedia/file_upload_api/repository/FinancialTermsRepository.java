package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.FinancialTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialTermsRepository extends JpaRepository<FinancialTerms, Long> {
    List<FinancialTerms> findByCompany_CompanyId(Long companyId);
    List<FinancialTerms> findByCompany_CompanyIdAndIsActive(Long companyId, Boolean isActive);
} 