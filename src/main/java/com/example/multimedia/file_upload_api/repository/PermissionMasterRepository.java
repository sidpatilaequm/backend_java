package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PermissionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionMasterRepository extends JpaRepository<PermissionMaster, Long> {
    Optional<PermissionMaster> findByCode(String code);
}
