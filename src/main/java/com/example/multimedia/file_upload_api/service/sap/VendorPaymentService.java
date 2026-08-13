package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.VendorPayment;

import java.time.LocalDate;
import java.util.List;

public interface VendorPaymentService {
    SapSyncResponse syncVendorPayments();
    
    List<VendorPayment> getVendorPayments(String status, String method, LocalDate from, LocalDate to);
    
    VendorPayment getVendorPaymentDetails(String documentNumber);
    
    List<VendorPayment> getAdminPayments();
    
    List<VendorPayment> getAdminPaymentsForVendor(Long vendorId);
}
