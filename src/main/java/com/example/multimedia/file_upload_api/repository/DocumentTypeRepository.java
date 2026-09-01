package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, String> {
    List<DocumentType> findByIsActiveTrueOrderByCode();
}
