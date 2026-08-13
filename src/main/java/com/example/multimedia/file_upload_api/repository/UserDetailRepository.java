package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByEmail(String email);
    Optional<UserDetail> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<UserDetail> findByOnboardingToken(String onboardingToken);
    List<UserDetail> findBySuperAdminAndOnboardingStatus(SuperAdmin superAdmin, String onboardingStatus);
    List<UserDetail> findBySuperAdmin(SuperAdmin superAdmin);
} 