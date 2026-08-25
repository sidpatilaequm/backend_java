package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionRequest;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionStatusRequest;
import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PurchaseRequisitionService {
    PurchaseRequisitionResponse createPurchaseRequisition(PurchaseRequisitionRequest request);

    Page<PurchaseRequisitionResponse> getAllPurchaseRequisitions(Long locationId, PurchaseRequisitionStatus status,
            String search, Pageable pageable);

    PurchaseRequisitionResponse getPurchaseRequisitionById(Long id);    

    PurchaseRequisitionResponse updatePurchaseRequisition(Long id, PurchaseRequisitionRequest request);

    void createRfq(Long prId, java.util.List<Long> vendorIds);

    void changePurchaseRequisitionStatus(Long id, PurchaseRequisitionStatusRequest statusRequest);

    void deletePurchaseRequisition(Long id);

    PurchaseRequisitionResponse getPurchaseRequisitionByPrNumber(String prNumber);

    List<PurchaseRequisitionResponse> getAllVendorPurchaseRequisitions(Long vendorId);

    PurchaseRequisitionResponse getVendorPurchaseRequisitionByPrNumber(String prNumber, Long vendorId);

    List<com.example.multimedia.file_upload_api.dto.VendorPurchaseRequisitionItemResponse> getVendorAssignedItems(Long vendorId);

    void respondToPurchaseRequisition(Long prId, Long vendorId, String action);

    List<PurchaseRequisitionResponse> getAcceptedVendorPurchaseRequisitions(Long vendorId);

    PurchaseRequisitionResponse getAcceptedVendorPurchaseRequisitionById(Long prId, Long vendorId);
}
