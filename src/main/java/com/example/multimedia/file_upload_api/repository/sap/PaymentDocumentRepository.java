package com.example.multimedia.file_upload_api.repository.sap;

import com.example.multimedia.file_upload_api.entity.sap.PaymentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentDocumentRepository extends JpaRepository<PaymentDocument, Long> {
    List<PaymentDocument> findByPaymentId(Long paymentId);
}
