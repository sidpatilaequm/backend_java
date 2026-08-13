package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.RolePermission;
import com.example.multimedia.file_upload_api.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(UserType role);

    Optional<RolePermission> findByRoleAndPermissionId(UserType role, Long permissionId);

    void deleteByRole(UserType role);
}
