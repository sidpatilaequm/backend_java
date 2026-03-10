package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.FinancialTermsCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialTermsCustomerRepository extends JpaRepository<FinancialTermsCustomer, Long> {
    List<FinancialTermsCustomer> findByCompany_CompanyId(Long companyId);
    List<FinancialTermsCustomer> findByCompany_CompanyIdAndIsActive(Long companyId, Boolean isActive);
} 