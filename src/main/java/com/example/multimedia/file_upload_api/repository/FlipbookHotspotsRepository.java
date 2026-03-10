package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.FlipbookHotspots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlipbookHotspotsRepository extends JpaRepository<FlipbookHotspots, Long> {
    Optional<FlipbookHotspots> findFirstByDocKeyAndSuperAdmin_SuperAdminIdOrderByVersionDesc(String docKey, Long superAdminId);
    Optional<FlipbookHotspots> findByDocKeyAndVersionAndSuperAdmin_SuperAdminId(String docKey, Integer version, Long superAdminId);
    List<FlipbookHotspots> findByDocKeyAndSuperAdmin_SuperAdminId(String docKey, Long superAdminId);
}


