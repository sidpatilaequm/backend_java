package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchasingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchasingDataRepository extends JpaRepository<PurchasingData, Long> {
    PurchasingData findByCompany_CompanyId(Long companyId);
} 