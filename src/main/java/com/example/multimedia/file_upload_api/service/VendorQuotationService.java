package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.VendorQuotationRequest;
import com.example.multimedia.file_upload_api.dto.VendorQuotationResponse;
import com.example.multimedia.file_upload_api.dto.VendorQuotationComparisonResponse;

import java.util.List;

public interface VendorQuotationService {
    VendorQuotationResponse createQuotation(VendorQuotationRequest request, Long vendorId);
    List<VendorQuotationResponse> getQuotationsByVendorId(Long vendorId, String companyCode);
    VendorQuotationResponse getQuotationById(Long id, Long vendorId);
    VendorQuotationResponse getQuotationByIdForAdmin(Long id);
    VendorQuotationResponse getQuotationByQuotationNumber(String quotationNumber);
    VendorQuotationResponse getQuotationByQuotationNumberAndVendorId(String quotationNumber, Long vendorId);
    List<VendorQuotationResponse> getAwardedQuotationsForAdmin(Long adminId);
    List<VendorQuotationResponse> getAllQuotationsByVendorIdForAdmin(Long vendorId);
    List<VendorQuotationResponse> getAllQuotationsByPrIdForAdmin(Long prId);
    void awardQuotation(Long quotationId, String remarks);
    List<VendorQuotationComparisonResponse> getQuotationComparison(Long prId);
}
