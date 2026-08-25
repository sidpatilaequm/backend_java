package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SupplierRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRegistrationRepository extends JpaRepository<SupplierRegistration, Long> {
    Optional<SupplierRegistration> findByResumeCode(String resumeCode);

    List<SupplierRegistration> findByStatusOrderByApprovedDateDesc(String status);

    Optional<SupplierRegistration> findByEmail(String email);

    Optional<SupplierRegistration> findByWorkflowRequestId(Long workflowRequestId);

    long countByStatusAndDynamicQuestionnaireProcessId(String status, Integer dynamicQuestionnaireProcessId);

    /**
     * Atomically claims "who decides the vendor's classification" — only the first approver to
     * call this actually sets it; the WHERE clause makes every later caller's UPDATE match zero
     * rows once the first one commits, so concurrent approvers can never race each other into
     * overwriting the classification (same pattern as SupplierChangeRequestRepository.markDecided).
     */
    @Modifying
    @Query("UPDATE SupplierRegistration r SET r.vendorCategory = :category WHERE r.id = :id AND r.vendorCategory IS NULL")
    int claimVendorCategory(@Param("id") Long id, @Param("category") String category);
}
