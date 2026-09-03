package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchaseRequisition;
import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PurchaseRequisition> findTopByOrderByIdDesc();

    Optional<PurchaseRequisition> findByPrNumber(String prNumber);

    // Unrestricted PR-number search for the Audit Log's PR Lifecycle tab — unlike
    // findWithFilters/findWithFiltersIn below, this isn't scoped to a submitter, since an admin
    // browsing the audit trail needs to find any PR, not just their own.
    List<PurchaseRequisition> findTop20ByPrNumberContainingIgnoreCaseOrderByCreatedAtDesc(String search);

    @Query("SELECT p FROM PurchaseRequisition p WHERE p.requestedBy = :requestedBy " +
            "AND (:locationId IS NULL OR p.locationId = :locationId) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR LOWER(p.prNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseRequisition> findWithFilters(
            @Param("requestedBy") Long requestedBy,
            @Param("locationId") Long locationId,
            @Param("status") PurchaseRequisitionStatus status,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT p FROM PurchaseRequisition p WHERE p.requestedBy IN :requestedByIds " +
            "AND (:locationId IS NULL OR p.locationId = :locationId) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR LOWER(p.prNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseRequisition> findWithFiltersIn(
            @Param("requestedByIds") java.util.Collection<Long> requestedByIds,
            @Param("locationId") Long locationId,
            @Param("status") PurchaseRequisitionStatus status,
            @Param("search") String search,
            Pageable pageable);
}

