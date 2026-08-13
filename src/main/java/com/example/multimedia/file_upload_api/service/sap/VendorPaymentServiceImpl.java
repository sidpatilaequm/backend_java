package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.PaymentSyncLog;
import com.example.multimedia.file_upload_api.entity.sap.VendorPayment;
import com.example.multimedia.file_upload_api.repository.sap.PaymentSyncLogRepository;
import com.example.multimedia.file_upload_api.repository.sap.VendorPaymentRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.util.SecurityContextUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VendorPaymentServiceImpl implements VendorPaymentService {

    @Autowired
    private VendorPaymentRepository paymentRepository;

    @Autowired
    private PaymentSyncLogRepository syncLogRepository;

    @Autowired
    private SecurityContextUtils securityContextUtils;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Override
    public SapSyncResponse syncVendorPayments() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        
        PaymentSyncLog syncLog = new PaymentSyncLog();
        syncLog.setVendorId(vendorId);
        syncLog.setSyncType("FBL1N_PAYR");
        syncLog.setSapApiName("SAP_PAYMENT_API");
        syncLog.setStartedAt(LocalDateTime.now());
        syncLog.setSyncStatus("IN_PROGRESS");
        syncLog = syncLogRepository.save(syncLog);

        try {
            // MOCK SAP API CALL
            VendorPayment mockPayment = new VendorPayment();
            mockPayment.setVendorId(vendorId);
            mockPayment.setCompanyCode("1000");
            mockPayment.setDocumentNumber("PAY-" + System.currentTimeMillis());
            mockPayment.setFiscalYear(String.valueOf(LocalDate.now().getYear()));
            mockPayment.setInvoiceReference("INV-" + System.currentTimeMillis());
            mockPayment.setInvoiceDate(LocalDate.now().minusDays(10));
            mockPayment.setPaymentDate(LocalDate.now());
            mockPayment.setGrossAmount(new BigDecimal("10500.00"));
            mockPayment.setTdsDeducted(new BigDecimal("500.00"));
            mockPayment.setNetPaid(new BigDecimal("10000.00"));
            mockPayment.setCurrency("INR");
            mockPayment.setPaymentMethod("RTGS");
            mockPayment.setUtrChequeNumber("UTR" + UUID.randomUUID().toString().substring(0, 8));
            mockPayment.setPaymentStatus("PAID");
            mockPayment.setSyncedAt(LocalDateTime.now());
            
            paymentRepository.save(mockPayment);

            syncLog.setRecordsFetched(1);
            syncLog.setSyncStatus("SUCCESS");
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);

            return new SapSyncResponse("SUCCESS", "Payment Sync Completed", 1);
        } catch (Exception e) {
            syncLog.setSyncStatus("FAILED");
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            throw new RuntimeException("Payment sync failed: " + e.getMessage());
        }
    }

    @Override
    public List<VendorPayment> getVendorPayments(String status, String method, LocalDate from, LocalDate to) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        
        Specification<VendorPayment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("vendorId"), vendorId));
            
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("paymentStatus"), status));
            }
            if (method != null && !method.isEmpty()) {
                predicates.add(cb.equal(root.get("paymentMethod"), method));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), to));
            }
            
            query.orderBy(cb.desc(root.get("paymentDate")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return paymentRepository.findAll(spec);
    }

    @Override
    public VendorPayment getVendorPaymentDetails(String documentNumber) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return paymentRepository.findByDocumentNumberAndVendorId(documentNumber, vendorId)
                .orElseThrow(() -> new RuntimeException("Payment document not found or unauthorized"));
    }

    @Override
    public List<VendorPayment> getAdminPayments() {
        Long adminId = securityContextUtils.getCurrentAdminId();
        
        List<Long> mappedVendorIds = companyDetailsRepository.findBySuperAdminSuperAdminId(adminId).stream()
                .map(CompanyDetails::getCompanyId)
                .collect(Collectors.toList());

        if (mappedVendorIds.isEmpty()) {
            return List.of();
        }
        
        return paymentRepository.findByVendorIdInOrderByPaymentDateDesc(mappedVendorIds);
    }

    @Override
    public List<VendorPayment> getAdminPaymentsForVendor(Long vendorId) {
        Long adminId = securityContextUtils.getCurrentAdminId();
        
        boolean isMapped = companyDetailsRepository.findBySuperAdminSuperAdminId(adminId).stream()
                .anyMatch(c -> c.getCompanyId().equals(vendorId));

        if (!isMapped) {
            throw new RuntimeException("Unauthorized to view this vendor's payments");
        }
        
        return paymentRepository.findByVendorIdOrderByPaymentDateDesc(vendorId);
    }
}
