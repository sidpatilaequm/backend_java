package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Rfq;
import com.example.multimedia.file_upload_api.entity.RfqVendor;
import com.example.multimedia.file_upload_api.entity.VendorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfqVendorRepository extends JpaRepository<RfqVendor, Long> {

    /** All vendor entries for a given RFQ. */
    List<RfqVendor> findByRfq(Rfq rfq);

    /** All vendor entries for a given RFQ id. */
    List<RfqVendor> findByRfq_RfqId(Long rfqId);

    /** All RFQ assignments for a specific vendor (used to show vendor their RFQs). */
    List<RfqVendor> findByVendor(VendorMaster vendor);

    /** All RFQ assignments for a specific vendor id. */
    List<RfqVendor> findByVendor_VendorId(Long vendorId);

    /** Check if a vendor is already assigned to an RFQ (for duplicate guard). */
    boolean existsByRfqAndVendor(Rfq rfq, VendorMaster vendor);

    /** Single entry for a specific RFQ + vendor combination. */
    Optional<RfqVendor> findByRfqAndVendor(Rfq rfq, VendorMaster vendor);

    /**
     * Find all RFQs assigned to a vendor, joining up to the PR.
     * Used to replace the old purchase-requisitions endpoint for vendors.
     */
    @Query("SELECT rv FROM RfqVendor rv " +
           "JOIN FETCH rv.rfq r " +
           "JOIN FETCH r.purchaseRequisition pr " +
           "WHERE rv.vendor.vendorId = :vendorId")
    List<RfqVendor> findByVendorIdWithRfqAndPr(@Param("vendorId") Long vendorId);
}
