package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchaseRequisition;
import com.example.multimedia.file_upload_api.entity.Rfq;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfqRepository extends JpaRepository<Rfq, Long> {

    /** Find RFQ by its human-readable number (e.g. "RFQ-2026-0001"). */
    Optional<Rfq> findByRfqNumber(String rfqNumber);

    /** All RFQs created for a specific PR. */
    List<Rfq> findByPurchaseRequisition(PurchaseRequisition purchaseRequisition);

    /** All RFQs created for a specific PR by ID. */
    List<Rfq> findByPurchaseRequisition_Id(Long prId);

    /** All RFQs with a given status (DRAFT / SENT / CLOSED). */
    List<Rfq> findByStatus(String status);

    /** All RFQs created by a specific Purchase Dept user. */
    List<Rfq> findByCreatedBy(UserDetail createdBy);

    /** All RFQs for a PR filtered by status. */
    List<Rfq> findByPurchaseRequisition_IdAndStatus(Long prId, String status);

    /** Count RFQs to help generate the next RFQ number. */
    long countByRfqNumberStartingWith(String prefix);
}
