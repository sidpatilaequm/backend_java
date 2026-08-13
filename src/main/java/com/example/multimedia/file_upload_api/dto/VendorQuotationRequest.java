package com.example.multimedia.file_upload_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class VendorQuotationRequest {

    @JsonProperty("pr_id")
    private Long prId;

    @JsonProperty("quotation_header")
    private QuotationHeader quotationHeader;

    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms;

    @JsonProperty("delivery_details")
    private DeliveryDetails deliveryDetails;

    @JsonProperty("freight_details")
    private FreightDetails freightDetails;

    @JsonProperty("remarks")
    private Remarks remarks;

    @JsonProperty("line_items")
    private List<LineItem> lineItems;

    @JsonProperty("documents")
    private Documents documents;

    @Data
    public static class QuotationHeader {
        @JsonProperty("quotation_number")
        private String quotationNumber;
        @JsonProperty("quotation_date")
        private LocalDate quotationDate;
        @JsonProperty("vendor_reference_no")
        private String vendorReferenceNo;
        @JsonProperty("currency")
        private String currency;
        @JsonProperty("validity_days")
        private Integer validityDays;
        @JsonProperty("valid_until")
        private LocalDate validUntil;
    }

    @Data
    public static class PaymentTerms {
        @JsonProperty("payment_terms_id")
        private Long paymentTermsId;
        @JsonProperty("advance_required_percent")
        private BigDecimal advanceRequiredPercent;
        @JsonProperty("bank_guarantee_required")
        private Boolean bankGuaranteeRequired;
    }

    @Data
    public static class DeliveryDetails {
        @JsonProperty("incoterm")
        private String incoterm;
        @JsonProperty("named_place")
        private String namedPlace;
        @JsonProperty("quoted_delivery_date")
        private LocalDate quotedDeliveryDate;
        @JsonProperty("lead_time_days")
        private Integer leadTimeDays;
        @JsonProperty("shipping_mode")
        private String shippingMode;
    }

    @Data
    public static class FreightDetails {
        @JsonProperty("freight_charge_type")
        private String freightChargeType;
        @JsonProperty("freight_amount")
        private BigDecimal freightAmount;
    }

    @Data
    public static class Remarks {
        @JsonProperty("cover_note")
        private String coverNote;
        @JsonProperty("internal_notes")
        private String internalNotes;
    }

    @Data
    public static class LineItem {
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
    }

    @Data
    public static class Documents {
        @JsonProperty("quotation_pdf")
        private String quotationPdf;
        @JsonProperty("technical_specification")
        private List<DocumentMeta> technicalSpecification;
        @JsonProperty("quality_certificate")
        private List<DocumentMeta> qualityCertificate;
        @JsonProperty("product_brochure")
        private List<DocumentMeta> productBrochure;
        @JsonProperty("other_documents")
        private List<DocumentMeta> otherDocuments;
    }
    
    @Data
    public static class DocumentMeta {
        @JsonProperty("file_name")
        private String fileName;
        @JsonProperty("file_size")
        private Long fileSize;
        @JsonProperty("file_type")
        private String fileType;
        @JsonProperty("file_path")
        private String filePath;
    }
}
