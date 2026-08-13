package com.example.multimedia.file_upload_api.repository.sap;

import com.example.multimedia.file_upload_api.entity.sap.ServicePurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePurchaseOrderRepository extends JpaRepository<ServicePurchaseOrder, Long> {
    List<ServicePurchaseOrder> findByVendorIdOrderBySyncedAtDesc(Long vendorId);
    List<ServicePurchaseOrder> findByVendorIdInOrderBySyncedAtDesc(List<Long> vendorIds);
    Optional<ServicePurchaseOrder> findByServicePoNumberAndVendorId(String servicePoNumber, Long vendorId);
    Optional<ServicePurchaseOrder> findByServicePoNumberAndVendorIdIn(String servicePoNumber, List<Long> vendorIds);
}
