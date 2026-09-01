package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchaseRoleGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRoleGrantRepository
        extends JpaRepository<PurchaseRoleGrant, PurchaseRoleGrant.Pk> {
    List<PurchaseRoleGrant> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
}
