package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchasingGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasingGroupRepository extends JpaRepository<PurchasingGroup, String> {
    List<PurchasingGroup> findByPurchasingOrg_PurchOrgCode(String purchOrgCode);
}
