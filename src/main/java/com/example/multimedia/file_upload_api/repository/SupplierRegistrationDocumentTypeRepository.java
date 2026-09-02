package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SupplierRegistrationDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRegistrationDocumentTypeRepository
        extends JpaRepository<SupplierRegistrationDocumentType, SupplierRegistrationDocumentType.Pk> {
    List<SupplierRegistrationDocumentType> findByRegistrationId(Long registrationId);
}
