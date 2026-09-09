package com.example.multimedia.file_upload_api.enums;

/**
 * The 5 SAP-exported report excels (Vendor Payments, Vendor Returns, Credit Notes, Invoices,
 * Vendor Stock) plus ASN, and the real table(s) each one maps to. Most are a single flat table;
 * INVOICES and ASN have a header table plus a line-item child table (itemTable, nullable) —
 * the mapping UI shows both, grouped, and a saved column can come from either one.
 */
public enum ExcelReportType {
    VENDOR_PAYMENTS("vendor_payments", "Vendor Payments", null),
    VENDOR_RETURNS("vendor_returns", "Vendor Returns", null),
    CREDIT_NOTES("vendor_credit_notes", "Credit Notes", null),
    INVOICES("vendor_invoice", "Invoices", "vendor_invoice_item"),
    VENDOR_STOCK("current_stock", "Vendor Stock", null),
    ASN("asns", "Advance Shipment Notice", "asn_items");

    private final String targetTable;
    private final String label;
    private final String itemTable;

    ExcelReportType(String targetTable, String label, String itemTable) {
        this.targetTable = targetTable;
        this.label = label;
        this.itemTable = itemTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public String getLabel() {
        return label;
    }

    public String getItemTable() {
        return itemTable;
    }
}
