package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.VendorAddress;
import com.example.multimedia.file_upload_api.entity.VendorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorAddressRepository extends JpaRepository<VendorAddress, Long> {
    
    Optional<VendorAddress> findByAddressId(String addressId);
    
    boolean existsByAddressId(String addressId);
    
    List<VendorAddress> findByVendorMaster(VendorMaster vendorMaster);
}
