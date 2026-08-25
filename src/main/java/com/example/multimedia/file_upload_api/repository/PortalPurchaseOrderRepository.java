package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortalPurchaseOrderRepository extends JpaRepository<PortalPurchaseOrder, Long> {

    List<PortalPurchaseOrder> findByVendor_SuperAdmin_SuperAdminIdOrderByIdDesc(Long adminId);

    List<PortalPurchaseOrder> findByVendor_CompanyIdOrderByIdDesc(Long vendorId);

    List<PortalPurchaseOrder> findByVendor_CompanyCodeOrderByIdDesc(String companyCode);

    Optional<PortalPurchaseOrder> findByIdAndVendor_CompanyId(Long id, Long vendorId);
    
    Optional<PortalPurchaseOrder> findByPoNumber(String poNumber);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT p FROM PortalPurchaseOrder p WHERE p.poNumber = :poNumber AND p.vendor.companyCode = :vendorBpno")
    Optional<PortalPurchaseOrder> findByPoNumberAndVendorBpnoForUpdate(@org.springframework.data.repository.query.Param("poNumber") String poNumber, @org.springframework.data.repository.query.Param("vendorBpno") String vendorBpno);

    Optional<PortalPurchaseOrder> findByPoNumberAndVendor_CompanyId(String poNumber, Long vendorId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PortalPurchaseOrder p WHERE p.purchaseRequisition.requestedBy IN :requestedByIds ORDER BY p.createdDate DESC")
    List<PortalPurchaseOrder> findByPurchaseRequisition_RequestedByIn(java.util.Collection<Long> requestedByIds);
}
