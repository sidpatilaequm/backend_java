package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.PurchaseRequisition;

import java.util.List;

public interface PurchaseRequisitionService {
    SapSyncResponse syncPurchaseRequisitions();
    List<PurchaseRequisition> getVendorPurchaseRequisitions();
    PurchaseRequisition getVendorPurchaseRequisition(String prNumber);
    List<PurchaseRequisition> getAdminPurchaseRequisitions();
    PurchaseRequisition getAdminPurchaseRequisition(String prNumber);
}
