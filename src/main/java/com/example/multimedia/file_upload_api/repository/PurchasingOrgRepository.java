package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchasingOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasingOrgRepository extends JpaRepository<PurchasingOrg, String> {
    List<PurchasingOrg> findByCompany_CompanyCode(String companyCode);
}
