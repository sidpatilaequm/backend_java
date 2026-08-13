package com.example.multimedia.file_upload_api.repository.sap;

import com.example.multimedia.file_upload_api.entity.sap.PurchaseRequisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("sapPurchaseRequisitionRepository")
public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Long> {
    List<PurchaseRequisition> findByVendorIdOrderBySyncedAtDesc(Long vendorId);
    List<PurchaseRequisition> findByVendorIdInOrderBySyncedAtDesc(List<Long> vendorIds);
    Optional<PurchaseRequisition> findByPrNumberAndVendorId(String prNumber, Long vendorId);
    Optional<PurchaseRequisition> findByPrNumberAndVendorIdIn(String prNumber, List<Long> vendorIds);
}
