package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.VendorQuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorQuotationItemRepository extends JpaRepository<VendorQuotationItem, Long> {
}
