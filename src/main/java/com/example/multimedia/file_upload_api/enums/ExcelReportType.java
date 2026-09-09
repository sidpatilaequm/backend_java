package com.example.multimedia.file_upload_api.enums;

/**
 * The 5 SAP-exported report excels (Vendor Payments, Vendor Returns, Credit Notes, Invoices,
 * Vendor Stock) and the real table each one is meant to land in. FolderIt watching/cron import
 * is a later phase — for now this enum only backs the column-mapping config screen.
 */
public enum ExcelReportType {
    VENDOR_PAYMENTS("vendor_payments", "Vendor Payments"),
    VENDOR_RETURNS("vendor_returns", "Vendor Returns"),
    CREDIT_NOTES("vendor_credit_notes", "Credit Notes"),
    INVOICES("vendor_invoice", "Invoices"),
    VENDOR_STOCK("current_stock", "Vendor Stock");

    private final String targetTable;
    private final String label;

    ExcelReportType(String targetTable, String label) {
        this.targetTable = targetTable;
        this.label = label;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public String getLabel() {
        return label;
    }
}
