package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.MasterBomFile;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterBomFileRepository extends JpaRepository<MasterBomFile, Long> {

    Optional<MasterBomFile> findTopByIsActiveAndSuperAdminOrderByIdDesc(Boolean isActive, SuperAdmin superAdmin);

    Optional<MasterBomFile> findTopBySuperAdminOrderByIdDesc(SuperAdmin superAdmin);

    List<MasterBomFile> findAllBySuperAdminOrderByIdDesc(SuperAdmin superAdmin);

    @Modifying
    @Query("UPDATE MasterBomFile m SET m.isActive = false WHERE m.isActive = true AND m.superAdmin = :superAdmin")
    int invalidateAllActiveFiles(@Param("superAdmin") SuperAdmin superAdmin);
}
