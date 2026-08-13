package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.ServicePurchaseOrder;

import java.util.List;

public interface ServicePurchaseOrderService {
    SapSyncResponse syncServicePurchaseOrders();
    List<ServicePurchaseOrder> getVendorServicePurchaseOrders();
    ServicePurchaseOrder getVendorServicePurchaseOrder(String servicePoNumber);
    List<ServicePurchaseOrder> getAdminServicePurchaseOrders();
    ServicePurchaseOrder getAdminServicePurchaseOrder(String servicePoNumber);
}
