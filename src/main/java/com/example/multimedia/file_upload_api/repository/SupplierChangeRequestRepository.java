package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SupplierChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierChangeRequestRepository extends JpaRepository<SupplierChangeRequest, Long> {
    List<SupplierChangeRequest> findByRegistrationIdOrderByCreatedDateDesc(Long registrationId);
    Optional<SupplierChangeRequest> findByWorkflowRequestId(Long workflowRequestId);
}
