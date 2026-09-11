package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.Asn;
import com.example.multimedia.file_upload_api.entity.AsnItem;
import com.example.multimedia.file_upload_api.entity.GateEntry;
import com.example.multimedia.file_upload_api.entity.GateEntryLine;
import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrder;
import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrderItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GoodsReceiptExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public byte[] generateGoodsReceiptExcel(GateEntry ge) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle subTitleStyle = createSubtitleStyle(wb);
            CellStyle sectionHeaderStyle = createSectionHeaderStyle(wb);
            CellStyle labelStyle = createLabelStyle(wb);
            CellStyle valueStyle = createValueStyle(wb);
            CellStyle tableHeaderStyle = createTableHeaderStyle(wb);
            CellStyle dataCellStyle = createDataCellStyle(wb);
            CellStyle numberCellStyle = createNumberCellStyle(wb);

            Asn asn = ge.getAsn();

            // ----------------------------------------------------
            // SHEET 1: Gate Entry & ASN Summary
            // ----------------------------------------------------
            Sheet sheet1 = wb.createSheet("Gate Entry & ASN Details");
            sheet1.setDisplayGridlines(true);

            int rowIdx = 0;

            // Title Banner
            Row titleRow = sheet1.createRow(rowIdx++);
            titleRow.setHeightInPoints(28);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("GATE ENTRY & GOODS RECEIPT SUMMARY PASS");
            titleCell.setCellStyle(titleStyle);
            sheet1.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            Row subRow = sheet1.createRow(rowIdx++);
            Cell subCell = subRow.createCell(0);
            subCell.setCellValue("Aecum India Private Limited — Procurement & Stores");
            subCell.setCellStyle(subTitleStyle);
            sheet1.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

            rowIdx++; // Empty row

            // Section 1: Gate Entry Metadata
            rowIdx = addSectionHeader(sheet1, rowIdx, "1. GATE ENTRY INFORMATION", sectionHeaderStyle);
            rowIdx = addKeyValueRow(sheet1, rowIdx, "Gate Pass Number", ge.getGatePassNumber() != null ? ge.getGatePassNumber() : "-", "Gate In Time", ge.getInTime() != null ? ge.getInTime().format(DATETIME_FMT) : "-", labelStyle, valueStyle);
            rowIdx = addKeyValueRow(sheet1, rowIdx, "Entry Status / Decision", ge.getDecision() != null ? ge.getDecision().toUpperCase() : "PENDING", "Processed By", ge.getProcessedBy() != null ? ge.getProcessedBy() : "System Security", labelStyle, valueStyle);
            rowIdx = addKeyValueRow(sheet1, rowIdx, "Packages Declared", String.valueOf(ge.getDeclaredPackages() != null ? ge.getDeclaredPackages() : "-"), "Packages Counted at Gate", String.valueOf(ge.getCountedPackages() != null ? ge.getCountedPackages() : "-"), labelStyle, valueStyle);
            if (ge.getPackageRemark() != null && !ge.getPackageRemark().isEmpty()) {
                rowIdx = addKeyValueRow(sheet1, rowIdx, "Package Discrepancy Note", ge.getPackageRemark(), "Supervisor Remark", ge.getSupervisorRemark() != null ? ge.getSupervisorRemark() : "-", labelStyle, valueStyle);
            }

            rowIdx++; // Empty row

            // Section 2: Consignment & Vendor Info
            rowIdx = addSectionHeader(sheet1, rowIdx, "2. CONSIGNMENT & VENDOR DETAILS", sectionHeaderStyle);
            String vendorName = "-";
            String vendorCode = asn != null && asn.getVendorBpno() != null ? asn.getVendorBpno() : "-";
            String poNo = "-";
            String poDateStr = "-";

            if (asn != null && asn.getPurchaseOrder() != null) {
                PortalPurchaseOrder po = asn.getPurchaseOrder();
                poNo = po.getPoNumber() != null ? po.getPoNumber() : "-";
                if (po.getPoDate() != null) {
                    poDateStr = po.getPoDate().format(DATE_FMT);
                }
                if (po.getVendor() != null && po.getVendor().getCompanyName() != null) {
                    vendorName = po.getVendor().getCompanyName();
                }
            }

            rowIdx = addKeyValueRow(sheet1, rowIdx, "Vendor Name", vendorName, "Vendor BP Code", vendorCode, labelStyle, valueStyle);
            rowIdx = addKeyValueRow(sheet1, rowIdx, "Purchase Order Ref", poNo, "PO Date", poDateStr, labelStyle, valueStyle);
            rowIdx = addKeyValueRow(sheet1, rowIdx, "Vehicle Number", asn != null && asn.getVehicleNumber() != null ? asn.getVehicleNumber() : "-", "Transporter Code", asn != null && asn.getTransporterCode() != null ? asn.getTransporterCode() : "-", labelStyle, valueStyle);

            rowIdx++; // Empty row

            // Section 3: ASN Header Information
            rowIdx = addSectionHeader(sheet1, rowIdx, "3. ADVANCE SHIPPING NOTICE (ASN) DETAILS", sectionHeaderStyle);
            if (asn != null) {
                String asnNo = String.format("ASN-%d-%04d", (asn.getCreatedDate() != null ? asn.getCreatedDate().getYear() : 2026), asn.getId());
                rowIdx = addKeyValueRow(sheet1, rowIdx, "ASN Reference No", asnNo, "Dispatch Date", asn.getDispatchDate() != null ? asn.getDispatchDate().format(DATE_FMT) : "-", labelStyle, valueStyle);
                rowIdx = addKeyValueRow(sheet1, rowIdx, "Tax Invoice Number", asn.getInvoiceNumber() != null ? asn.getInvoiceNumber() : "-", "Invoice Date", asn.getInvoiceDate() != null ? asn.getInvoiceDate().format(DATE_FMT) : "-", labelStyle, valueStyle);
                rowIdx = addKeyValueRow(sheet1, rowIdx, "e-Way Bill Number", asn.getEwayBill() != null ? asn.getEwayBill() : "-", "e-Way Bill Valid Upto", asn.getEwbValidTo() != null ? asn.getEwbValidTo().format(DATE_FMT) : "-", labelStyle, valueStyle);
                rowIdx = addKeyValueRow(sheet1, rowIdx, "Packaging Type", asn.getPackaging() != null ? asn.getPackaging() : "-", "Total Package Count", String.valueOf(asn.getNoOfPackages() != null ? asn.getNoOfPackages() : 0), labelStyle, valueStyle);
                rowIdx = addKeyValueRow(sheet1, rowIdx, "Expected Delivery", asn.getExpectedDelivery() != null ? asn.getExpectedDelivery().format(DATE_FMT) : "-", "Company Code", asn.getCompanyCode() != null ? asn.getCompanyCode() : "-", labelStyle, valueStyle);
            }

            // Auto-fit columns for Sheet 1
            sheet1.setColumnWidth(0, 7500);
            sheet1.setColumnWidth(1, 9500);
            sheet1.setColumnWidth(2, 7500);
            sheet1.setColumnWidth(3, 9500);

            // ----------------------------------------------------
            // SHEET 2: Shipped Material Lines
            // ----------------------------------------------------
            Sheet sheet2 = wb.createSheet("Shipped Material Lines");
            sheet2.setDisplayGridlines(true);

            int s2RowIdx = 0;
            Row s2Title = sheet2.createRow(s2RowIdx++);
            s2Title.setHeightInPoints(24);
            Cell s2TitleCell = s2Title.createCell(0);
            s2TitleCell.setCellValue("DESPATCHED MATERIALS & GATE COUNT BREAKOUT");
            s2TitleCell.setCellStyle(sectionHeaderStyle);
            sheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            s2RowIdx++; // Empty row

            // Table Header Row
            String[] headers = {
                "Line #", "Material Code / Part No", "Material Description", "UOM",
                "Qty Despatched", "Qty Counted at Gate", "Unit Price", "Total Line Value",
                "Batch / Heat No", "Line Remarks"
            };

            Row headerRow = sheet2.createRow(s2RowIdx++);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(tableHeaderStyle);
            }

            // Populate Item Rows
            if (asn != null && asn.getItems() != null && !asn.getItems().isEmpty()) {
                int lineNo = 1;
                for (AsnItem item : asn.getItems()) {
                    Row r = sheet2.createRow(s2RowIdx++);
                    r.createCell(0).setCellValue(lineNo++);
                    r.getCell(0).setCellStyle(dataCellStyle);

                    String partNo = item.getPartNumber() != null ? item.getPartNumber() : "-";
                    r.createCell(1).setCellValue(partNo);
                    r.getCell(1).setCellStyle(dataCellStyle);

                    String desc = "-";
                    String uom = "EA";
                    BigDecimal unitPrice = BigDecimal.ZERO;

                    if (item.getPurchaseOrderItem() != null) {
                        PortalPurchaseOrderItem poi = item.getPurchaseOrderItem();
                        if (poi.getMaterialDescription() != null) desc = poi.getMaterialDescription();
                        if (poi.getUom() != null) uom = poi.getUom();
                        if (poi.getUnitPrice() != null) unitPrice = poi.getUnitPrice();
                    }

                    r.createCell(2).setCellValue(desc);
                    r.getCell(2).setCellStyle(dataCellStyle);

                    r.createCell(3).setCellValue(uom);
                    r.getCell(3).setCellStyle(dataCellStyle);

                    double shippedQty = item.getQuantityShipped() != null ? item.getQuantityShipped().doubleValue() : 0.0;
                    Cell shippedCell = r.createCell(4);
                    shippedCell.setCellValue(shippedQty);
                    shippedCell.setCellStyle(numberCellStyle);

                    // Find counted qty from gate entry lines if available
                    double countedQty = shippedQty;
                    String lineRemark = "-";
                    if (ge.getLines() != null) {
                        for (GateEntryLine gel : ge.getLines()) {
                            if (partNo.equalsIgnoreCase(gel.getMaterialCode())) {
                                if (gel.getCountedQty() != null) countedQty = gel.getCountedQty().doubleValue();
                                if (gel.getRemark() != null) lineRemark = gel.getRemark();
                                break;
                            }
                        }
                    }

                    Cell countedCell = r.createCell(5);
                    countedCell.setCellValue(countedQty);
                    countedCell.setCellStyle(numberCellStyle);

                    Cell priceCell = r.createCell(6);
                    priceCell.setCellValue(unitPrice.doubleValue());
                    priceCell.setCellStyle(numberCellStyle);

                    double lineTotal = shippedQty * unitPrice.doubleValue();
                    Cell totalCell = r.createCell(7);
                    totalCell.setCellValue(lineTotal);
                    totalCell.setCellStyle(numberCellStyle);

                    r.createCell(8).setCellValue(item.getBatchHeatNumber() != null ? item.getBatchHeatNumber() : "-");
                    r.getCell(8).setCellStyle(dataCellStyle);

                    r.createCell(9).setCellValue(lineRemark);
                    r.getCell(9).setCellStyle(dataCellStyle);
                }
            }

            // Auto-size sheet2 columns
            for (int i = 0; i < headers.length; i++) {
                sheet2.autoSizeColumn(i);
                sheet2.setColumnWidth(i, Math.max(sheet2.getColumnWidth(i) + 1200, 3500));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private int addSectionHeader(Sheet sheet, int rowIdx, String title, CellStyle style) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(20);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 3));
        return rowIdx + 1;
    }

    private int addKeyValueRow(Sheet sheet, int rowIdx, String k1, String v1, String k2, String v2, CellStyle labelStyle, CellStyle valStyle) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(18);

        Cell c0 = row.createCell(0);
        c0.setCellValue(k1);
        c0.setCellStyle(labelStyle);

        Cell c1 = row.createCell(1);
        c1.setCellValue(v1 != null ? v1 : "-");
        c1.setCellStyle(valStyle);

        Cell c2 = row.createCell(2);
        c2.setCellValue(k2);
        c2.setCellStyle(labelStyle);

        Cell c3 = row.createCell(3);
        c3.setCellValue(v2 != null ? v2 : "-");
        c3.setCellStyle(valStyle);

        return rowIdx + 1;
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSectionHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createLabelStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 9);
        font.setBold(true);
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createValueStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTableHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataCellStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private CellStyle createNumberCellStyle(Workbook wb) {
        CellStyle style = createDataCellStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}
