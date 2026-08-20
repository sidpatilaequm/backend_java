package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SupplierChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierChangeRequestRepository extends JpaRepository<SupplierChangeRequest, Long> {
    List<SupplierChangeRequest> findByRegistrationIdOrderByCreatedDateDesc(Long registrationId);
    Optional<SupplierChangeRequest> findByWorkflowRequestId(Long workflowRequestId);

    /**
     * Atomically claims the "decide this request" step — WorkFlow's outgoing webhook is known to
     * fire more than once per decision (observed directly: two near-simultaneous deliveries both
     * reading status='PENDING' before either write committed), and a plain read-then-write would
     * apply the change and send the VCR.2 email twice. The WHERE clause makes only the first
     * caller's UPDATE match; MySQL's row lock on UPDATE serializes concurrent attempts, so the
     * loser reliably sees 0 rows affected once the winner commits, rather than a race on the read.
     */
    @Modifying
    @Query("UPDATE SupplierChangeRequest c SET c.status = :status, c.decidedDate = :decidedDate WHERE c.id = :id AND c.status = 'PENDING'")
    int markDecided(@Param("id") Long id, @Param("status") String status, @Param("decidedDate") LocalDateTime decidedDate);
}
