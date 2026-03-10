package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.FlipbookDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlipbookDocumentRepository extends JpaRepository<FlipbookDocument, Long> {
    Optional<FlipbookDocument> findByDocKeyAndSuperAdmin_SuperAdminId(String docKey, Long superAdminId);
    boolean existsByDocKeyAndSuperAdmin_SuperAdminId(String docKey, Long superAdminId);
    List<FlipbookDocument> findBySuperAdmin_SuperAdminId(Long superAdminId);
}


