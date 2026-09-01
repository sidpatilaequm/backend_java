package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.LoginAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Page<LoginAttempt> findBySuperAdmin_SuperAdminIdOrderByCreatedAtDesc(Long superAdminId, Pageable pageable);

    Page<LoginAttempt> findBySuperAdmin_SuperAdminIdAndSuccessOrderByCreatedAtDesc(
            Long superAdminId, boolean success, Pageable pageable);

    Page<LoginAttempt> findBySuperAdmin_SuperAdminIdAndMethodOrderByCreatedAtDesc(
            Long superAdminId, String method, Pageable pageable);

    Page<LoginAttempt> findBySuperAdmin_SuperAdminIdAndSuccessAndMethodOrderByCreatedAtDesc(
            Long superAdminId, boolean success, String method, Pageable pageable);
}
