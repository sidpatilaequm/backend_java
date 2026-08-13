package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.PurchaseOrder;

import java.util.List;

public interface PurchaseOrderService {
    SapSyncResponse syncPurchaseOrders();
    List<PurchaseOrder> getVendorPurchaseOrders();
    PurchaseOrder getVendorPurchaseOrder(String poNumber);
    List<PurchaseOrder> getAdminPurchaseOrders();
    PurchaseOrder getAdminPurchaseOrder(String poNumber);
}
