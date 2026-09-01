package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findBySuperAdmin_SuperAdminIdOrderByCreatedAtDesc(Long superAdminId, Pageable pageable);

    Page<AuditLog> findBySuperAdmin_SuperAdminIdAndTargetUserIdOrderByCreatedAtDesc(
            Long superAdminId, Long targetUserId, Pageable pageable);

    Page<AuditLog> findBySuperAdmin_SuperAdminIdAndActionOrderByCreatedAtDesc(
            Long superAdminId, String action, Pageable pageable);

    Page<AuditLog> findBySuperAdmin_SuperAdminIdAndTargetUserIdAndActionOrderByCreatedAtDesc(
            Long superAdminId, Long targetUserId, String action, Pageable pageable);
}
