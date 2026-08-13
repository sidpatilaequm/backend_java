package com.example.multimedia.file_upload_api.repository.sap;

import com.example.multimedia.file_upload_api.entity.sap.VendorPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorPaymentRepository extends JpaRepository<VendorPayment, Long>, JpaSpecificationExecutor<VendorPayment> {
    
    List<VendorPayment> findByVendorIdOrderByPaymentDateDesc(Long vendorId);
    
    List<VendorPayment> findByVendorIdInOrderByPaymentDateDesc(List<Long> vendorIds);
    
    Optional<VendorPayment> findByDocumentNumberAndVendorId(String documentNumber, Long vendorId);
    
    Optional<VendorPayment> findByVendorIdAndId(Long vendorId, Long id);
}
