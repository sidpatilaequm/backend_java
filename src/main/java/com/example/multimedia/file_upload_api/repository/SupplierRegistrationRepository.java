package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SupplierRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRegistrationRepository extends JpaRepository<SupplierRegistration, Long> {
    Optional<SupplierRegistration> findByResumeCode(String resumeCode);

    Optional<SupplierRegistration> findByEmail(String email);

    Optional<SupplierRegistration> findByWorkflowRequestId(Long workflowRequestId);

    long countByStatusAndDynamicQuestionnaireProcessId(String status, Integer dynamicQuestionnaireProcessId);
}
