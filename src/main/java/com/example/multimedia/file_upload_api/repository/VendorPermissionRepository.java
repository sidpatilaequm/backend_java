package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.VendorPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorPermissionRepository extends JpaRepository<VendorPermission, Long> {
    List<VendorPermission> findByCompanyCompanyId(Long companyId);
    Optional<VendorPermission> findByCompanyCompanyIdAndPermissionCode(Long companyId, String code);
}
