package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.DocumentTypeCompanyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTypeCompanyCodeRepository
        extends JpaRepository<DocumentTypeCompanyCode, DocumentTypeCompanyCode.Pk> {
    List<DocumentTypeCompanyCode> findByDocTypeCode(String docTypeCode);
    boolean existsByDocTypeCodeAndCompanyCode(String docTypeCode, String companyCode);
}
