package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchaseRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRoleRepository extends JpaRepository<PurchaseRole, Long> {
    List<PurchaseRole> findByIsActiveTrueOrderByRoleCode();
    List<PurchaseRole> findByIsActiveTrueAndAssigneeTypeOrderByRoleCode(String assigneeType);
    Optional<PurchaseRole> findByRoleCode(String roleCode);
    Optional<PurchaseRole> findByRoleCodeAndIdNot(String roleCode, Long id);
}
