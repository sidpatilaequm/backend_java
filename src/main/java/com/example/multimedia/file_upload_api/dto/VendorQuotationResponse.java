package com.example.multimedia.file_upload_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VendorQuotationResponse {

    @JsonProperty("quotation_id")
    private Long quotationId;

    @JsonProperty("pr_id")
    private Long prId;

    @JsonProperty("vendor_id")
    private Long vendorId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("subtotal")
    private BigDecimal subtotal;

    @JsonProperty("gst_total")
    private BigDecimal gstTotal;

    @JsonProperty("freight_total")
    private BigDecimal freightTotal;

    @JsonProperty("grand_total")
    private BigDecimal grandTotal;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("quotation_header")
    private VendorQuotationRequest.QuotationHeader quotationHeader;

    @JsonProperty("payment_terms")
    private VendorQuotationRequest.PaymentTerms paymentTerms;

    @JsonProperty("delivery_details")
    private VendorQuotationRequest.DeliveryDetails deliveryDetails;

    @JsonProperty("freight_details")
    private VendorQuotationRequest.FreightDetails freightDetails;

    @JsonProperty("remarks")
    private VendorQuotationRequest.Remarks remarks;

    @JsonProperty("line_items")
    private List<LineItemResponse> lineItems;

    @JsonProperty("documents")
    private VendorQuotationRequest.Documents documents;

    @Data
    public static class LineItemResponse {
        @JsonProperty("quotation_item_id")
        private Long quotationItemId;
        @JsonProperty("pr_line_id")
        private Long prLineId;
        @JsonProperty("item_code")
        private String itemCode;
        @JsonProperty("description")
        private String description;
        @JsonProperty("pr_qty")
        private BigDecimal prQty;
        @JsonProperty("quoted_qty")
        private BigDecimal quotedQty;
        @JsonProperty("uom")
        private String uom;
        @JsonProperty("unit_price")
        private BigDecimal unitPrice;
        @JsonProperty("gst_percent")
        private BigDecimal gstPercent;
        @JsonProperty("delivery_date")
        private LocalDate deliveryDate;
        @JsonProperty("payment_terms_id")
        private Long paymentTermsId;
        @JsonProperty("incoterm")
        private String incoterm;
        @JsonProperty("freight_amount")
        private BigDecimal freightAmount;
        @JsonProperty("line_total")
        private BigDecimal lineTotal;
        @JsonProperty("gst_amount")
        private BigDecimal gstAmount;
    }
}
