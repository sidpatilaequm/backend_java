package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SupplierRegistrationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRegistrationDocumentRepository extends JpaRepository<SupplierRegistrationDocument, Long> {
    List<SupplierRegistrationDocument> findByRegistrationId(Long registrationId);

    Optional<SupplierRegistrationDocument> findByRegistrationIdAndDocType(Long registrationId, String docType);
}
