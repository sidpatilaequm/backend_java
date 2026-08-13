package com.example.multimedia.file_upload_api.repository.sap;

import com.example.multimedia.file_upload_api.entity.sap.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByVendorIdOrderBySyncedAtDesc(Long vendorId);
    List<PurchaseOrder> findByVendorIdInOrderBySyncedAtDesc(List<Long> vendorIds);
    Optional<PurchaseOrder> findByPoNumberAndVendorId(String poNumber, Long vendorId);
    Optional<PurchaseOrder> findByPoNumberAndVendorIdIn(String poNumber, List<Long> vendorIds);
}
