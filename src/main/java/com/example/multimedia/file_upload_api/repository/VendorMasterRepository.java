package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.VendorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorMasterRepository extends JpaRepository<VendorMaster, Long> {
    
    Optional<VendorMaster> findByBpNo(String bpNo);

    boolean existsByBpNo(String bpNo);

    Optional<VendorMaster> findBySupplierRegistrationId(Long supplierRegistrationId);

    // Email lives on the linked SupplierRegistration now, not on this table directly — these
    // traverse that relation. A vendor with no linked registration (legacy/SAP-imported) simply
    // never matches, same as an empty result would have before.
    java.util.List<VendorMaster> findBySupplierRegistration_Email(String email);

    boolean existsBySupplierRegistration_Email(String email);
}
