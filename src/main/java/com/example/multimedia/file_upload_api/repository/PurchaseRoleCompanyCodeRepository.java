package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchaseRoleCompanyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRoleCompanyCodeRepository
        extends JpaRepository<PurchaseRoleCompanyCode, PurchaseRoleCompanyCode.Pk> {
    List<PurchaseRoleCompanyCode> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
}
