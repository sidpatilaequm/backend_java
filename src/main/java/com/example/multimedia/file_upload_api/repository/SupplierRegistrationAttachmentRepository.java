package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SupplierRegistrationAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRegistrationAttachmentRepository extends JpaRepository<SupplierRegistrationAttachment, Long> {
    List<SupplierRegistrationAttachment> findByRegistrationIdOrderByCreatedDateAsc(Long registrationId);
    Optional<SupplierRegistrationAttachment> findByRegistrationIdAndQuestionId(Long registrationId, Integer questionId);
}
