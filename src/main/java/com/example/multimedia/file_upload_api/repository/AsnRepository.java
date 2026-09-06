package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Asn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsnRepository extends JpaRepository<Asn, Long> {
    List<Asn> findByPurchaseOrder_Id(Long poId);
    List<Asn> findByPurchaseOrder_PoNumber(String poNumber);
    Optional<Asn> findByIdAndVendorBpno(Long id, String vendorBpno);
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Asn a WHERE a.vendorBpno = :vendorBpno AND (:companyCode IS NULL OR a.companyCode = :companyCode) ORDER BY a.id DESC")
    List<Asn> findByVendorBpno(@org.springframework.data.repository.query.Param("vendorBpno") String vendorBpno, @org.springframework.data.repository.query.Param("companyCode") String companyCode);
    
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Asn a WHERE a.purchaseOrder.vendor.companyId = :vendorId AND (:companyCode IS NULL OR a.companyCode = :companyCode) ORDER BY a.id DESC")
    List<Asn> findByVendorId(@org.springframework.data.repository.query.Param("vendorId") Long vendorId, @org.springframework.data.repository.query.Param("companyCode") String companyCode);
    
    List<Asn> findByCompanyCodeOrderByIdDesc(String companyCode);
}
