package com.example.multimedia.file_upload_api.repository.sap;

import com.example.multimedia.file_upload_api.entity.sap.SubconPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubconPurchaseOrderRepository extends JpaRepository<SubconPurchaseOrder, Long> {
    List<SubconPurchaseOrder> findByVendorIdOrderBySyncedAtDesc(Long vendorId);
    List<SubconPurchaseOrder> findByVendorIdInOrderBySyncedAtDesc(List<Long> vendorIds);
    Optional<SubconPurchaseOrder> findBySubconPoNumberAndVendorId(String subconPoNumber, Long vendorId);
    Optional<SubconPurchaseOrder> findBySubconPoNumberAndVendorIdIn(String subconPoNumber, List<Long> vendorIds);
}
