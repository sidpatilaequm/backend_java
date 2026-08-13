package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.VendorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorMasterRepository extends JpaRepository<VendorMaster, Long> {
    
    Optional<VendorMaster> findByBpNo(String bpNo);
    
    boolean existsByBpNo(String bpNo);
    
    java.util.List<VendorMaster> findByEmail(String email);
}
