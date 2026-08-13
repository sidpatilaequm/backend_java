package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PortalPurchaseOrderResponse {
    private Long poId;
    private String poNumber;
    private LocalDate poDate;

    private Long prId;
    private String prNumber;

    private Long quotationId;
    private String quotationNumber;

    private VendorInfo vendor;

    private String currency;

    private PaymentTermsInfo paymentTerms;

    private String deliveryAddress;

    private LocalDate requestedDeliveryDate;

    private LocalDate confirmedDeliveryDate;

    private String shippingInstructions;

    private String status;

    private BigDecimal subtotal;
    private BigDecimal gstTotal;
    private BigDecimal freightTotal;
    private BigDecimal grandTotal;

    private List<ItemInfo> items;

    private LocalDateTime createdAt;

    @Data
    public static class VendorInfo {
        private Long vendorId;
        private String vendorCode;
        private String vendorName;
        private String gstin;
    }

    @Data
    public static class PaymentTermsInfo {
        private Long paymentTermsId;
        private String name;
    }

    @Data
    public static class ItemInfo {
        private Integer lineNumber;
        private String materialNumber;
        private String materialDescription;
        private BigDecimal quantity;
        private String uom;
        private BigDecimal unitPrice;
        private BigDecimal netValue;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal totalValue;
        private BigDecimal receivedQuantity;
        private BigDecimal inTransitQuantity;
        private BigDecimal pendingQuantity;
    }
}
