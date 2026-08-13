package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class PortalPurchaseOrderRequest {
    private String deliveryAddress;
    private String shippingInstructions;
    private String remarks;
}
