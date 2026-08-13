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
    List<Asn> findByVendorBpno(String vendorBpno);
}
