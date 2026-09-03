package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PurchaseRequisitionItemVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRequisitionItemVendorRepository extends JpaRepository<PurchaseRequisitionItemVendor, Long> {
    
    @Query("SELECT v FROM PurchaseRequisitionItemVendor v JOIN FETCH v.purchaseRequisitionItem pri JOIN FETCH pri.purchaseRequisition pr WHERE v.vendorId = :vendorId AND (:companyCode IS NULL OR pr.companyCode = :companyCode) ORDER BY v.sentAt DESC")
    List<PurchaseRequisitionItemVendor> findByVendorIdWithDetails(@Param("vendorId") Long vendorId, @Param("companyCode") String companyCode);

    @Query("SELECT v FROM PurchaseRequisitionItemVendor v JOIN FETCH v.purchaseRequisitionItem pri JOIN FETCH pri.purchaseRequisition pr WHERE v.vendorId = :vendorId AND v.status = :status AND (:companyCode IS NULL OR pr.companyCode = :companyCode) ORDER BY v.sentAt DESC")
    List<PurchaseRequisitionItemVendor> findByVendorIdAndStatus(@Param("vendorId") Long vendorId, @Param("status") String status, @Param("companyCode") String companyCode);

    @Query("SELECT v FROM PurchaseRequisitionItemVendor v JOIN FETCH v.purchaseRequisitionItem pri JOIN FETCH pri.purchaseRequisition pr WHERE v.vendorId = :vendorId AND v.status = 'ACCEPTED' AND pr.id = :prId")
    List<PurchaseRequisitionItemVendor> findAcceptedByVendorIdAndPrId(@Param("vendorId") Long vendorId, @Param("prId") Long prId);

    @Query("SELECT v FROM PurchaseRequisitionItemVendor v WHERE v.vendorId = :vendorId AND v.purchaseRequisitionItem.purchaseRequisition.id = :prId")
    List<PurchaseRequisitionItemVendor> findByVendorIdAndPrId(@Param("vendorId") Long vendorId, @Param("prId") Long prId);

    // Every RFQ-sent/response row for a PR, across all vendors and items — for the PR Lifecycle tab.
    @Query("SELECT v FROM PurchaseRequisitionItemVendor v WHERE v.purchaseRequisitionItem.purchaseRequisition.id = :prId ORDER BY v.sentAt ASC")
    List<PurchaseRequisitionItemVendor> findByPurchaseRequisitionItem_PurchaseRequisition_Id(@Param("prId") Long prId);
}
