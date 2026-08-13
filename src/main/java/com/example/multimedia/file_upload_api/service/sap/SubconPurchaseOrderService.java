package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.SubconPurchaseOrder;

import java.util.List;

public interface SubconPurchaseOrderService {
    SapSyncResponse syncSubconPurchaseOrders();
    List<SubconPurchaseOrder> getVendorSubconPurchaseOrders();
    SubconPurchaseOrder getVendorSubconPurchaseOrder(String subconPoNumber);
    List<SubconPurchaseOrder> getAdminSubconPurchaseOrders();
    SubconPurchaseOrder getAdminSubconPurchaseOrder(String subconPoNumber);
}
