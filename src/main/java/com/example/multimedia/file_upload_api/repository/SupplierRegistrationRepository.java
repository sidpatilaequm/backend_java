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

    /**
     * Atomically claims "who provisions this registration's vendor account" — WorkFlow's
     * outgoing webhook is known to redeliver, and the old guard (a plain
     * "if (reg.getUserId() != null) return;" check) was a read-then-write race: two nearly
     * simultaneous webhook deliveries could both read userId==null before either had committed,
     * and both call provisionVendorAccount, sending two separate credential emails with two
     * different passwords. -1 is an impossible real userId, used purely as a claim marker;
     * provisionVendorAccount overwrites it with the real id once the account actually exists.
     * clearAutomatically forces the persistence context to drop its stale (pre-update) copy of
     * this row so the very next read sees the claim, not the value cached before this ran.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE SupplierRegistration r SET r.userId = -1 WHERE r.id = :id AND r.userId IS NULL")
    int claimProvisioning(@Param("id") Long id);
}
