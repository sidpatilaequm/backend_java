package com.example.multimedia.file_upload_api.repository.sap;

import com.example.multimedia.file_upload_api.entity.sap.PaymentSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentSyncLogRepository extends JpaRepository<PaymentSyncLog, Long> {
    List<PaymentSyncLog> findByVendorIdOrderByStartedAtDesc(Long vendorId);
}
