package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.VendorQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorQuotationRepository extends JpaRepository<VendorQuotation, Long> {
    List<VendorQuotation> findByVendor_CompanyId(Long vendorId);
    List<VendorQuotation> findByPurchaseRequisition_Id(Long prId);
    boolean existsByPurchaseRequisition_IdAndVendor_CompanyId(Long prId, Long vendorId);
    Optional<VendorQuotation> findByQuotationNumber(String quotationNumber);

    @Query("SELECT q FROM VendorQuotation q WHERE q.status = :status AND q.purchaseRequisition.requestedBy = :superAdminId")
    List<VendorQuotation> findByStatusAndRequestedBy(@Param("status") String status, @Param("superAdminId") Long superAdminId);
}
