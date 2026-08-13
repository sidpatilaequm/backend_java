package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderListResponse;
import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderRequest;
import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderResponse;

import java.util.List;

public interface PortalPurchaseOrderService {

    PortalPurchaseOrderResponse createPOFromAwardedQuotation(Long quotationId, PortalPurchaseOrderRequest request);

    List<PortalPurchaseOrderListResponse> getAllPOsForAdmin(Long adminId);

    PortalPurchaseOrderResponse getPODetailsForAdmin(Long poId, Long adminId);

    void cancelPO(Long poId, Long adminId);

    List<PortalPurchaseOrderListResponse> getPOsForVendor(Long vendorId);

    PortalPurchaseOrderResponse getPODetailsForVendor(Long poId, Long vendorId);
}
