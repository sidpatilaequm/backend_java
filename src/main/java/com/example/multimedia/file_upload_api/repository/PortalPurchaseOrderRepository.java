package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortalPurchaseOrderRepository extends JpaRepository<PortalPurchaseOrder, Long> {

    List<PortalPurchaseOrder> findByVendor_SuperAdmin_SuperAdminIdOrderByIdDesc(Long adminId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PortalPurchaseOrder p WHERE p.vendor.companyId = :vendorId AND (:companyCode IS NULL OR p.companyCode = :companyCode) ORDER BY p.id DESC")
    List<PortalPurchaseOrder> findByVendor_CompanyIdOrderByIdDesc(@org.springframework.data.repository.query.Param("vendorId") Long vendorId, @org.springframework.data.repository.query.Param("companyCode") String companyCode);

    List<PortalPurchaseOrder> findByVendor_CompanyCodeOrderByIdDesc(String companyCode);

    Optional<PortalPurchaseOrder> findByIdAndVendor_CompanyId(Long id, Long vendorId);
    
    Optional<PortalPurchaseOrder> findByPoNumber(String poNumber);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT p FROM PortalPurchaseOrder p WHERE p.poNumber = :poNumber AND p.vendor.companyCode = :vendorBpno")
    Optional<PortalPurchaseOrder> findByPoNumberAndVendorBpnoForUpdate(@org.springframework.data.repository.query.Param("poNumber") String poNumber, @org.springframework.data.repository.query.Param("vendorBpno") String vendorBpno);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT p FROM PortalPurchaseOrder p WHERE p.poNumber = :poNumber AND p.vendor.companyId = :vendorId")
    Optional<PortalPurchaseOrder> findByPoNumberAndVendorCompanyIdForUpdate(@org.springframework.data.repository.query.Param("poNumber") String poNumber, @org.springframework.data.repository.query.Param("vendorId") Long vendorId);

    Optional<PortalPurchaseOrder> findByPoNumberAndVendor_CompanyId(String poNumber, Long vendorId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PortalPurchaseOrder p LEFT JOIN p.purchaseRequisition pr WHERE pr.requestedBy IN :requestedByIds OR p.createdBy IN :createdByList ORDER BY p.createdDate DESC")
    List<PortalPurchaseOrder> findByPurchaseRequisition_RequestedByInOrCreatedByIn(@org.springframework.data.repository.query.Param("requestedByIds") java.util.Collection<Long> requestedByIds, @org.springframework.data.repository.query.Param("createdByList") java.util.Collection<String> createdByList);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PortalPurchaseOrder p LEFT JOIN p.purchaseRequisition pr WHERE p.vendor.superAdmin.superAdminId = :superAdminId AND (pr.requestedBy IN :requestedByIds OR p.createdBy IN :createdByList OR p.createdBy IS NULL) ORDER BY p.createdDate DESC")
    List<PortalPurchaseOrder> findEmployeePOs(
        @org.springframework.data.repository.query.Param("superAdminId") Long superAdminId,
        @org.springframework.data.repository.query.Param("requestedByIds") java.util.Collection<Long> requestedByIds,
        @org.springframework.data.repository.query.Param("createdByList") java.util.Collection<String> createdByList
    );
}
