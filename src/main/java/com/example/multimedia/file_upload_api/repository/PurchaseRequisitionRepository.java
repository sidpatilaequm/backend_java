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

import java.util.Optional;

@Repository
public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PurchaseRequisition> findTopByOrderByIdDesc();

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
}
