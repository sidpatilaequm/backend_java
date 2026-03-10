package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    public byte[] generateExcelReport(Long userId) throws IOException {
        // Validate input parameters
        if (userId == null) {
            logger.error("User ID cannot be null");
            throw new RuntimeException("User ID is required");
        }

        // Find all company details for the user
        List<CompanyDetails> companyDetailsList = companyDetailsRepository.findByUserUserId(userId);
        if (companyDetailsList.isEmpty()) {
            logger.error("No company details found for userId: {}", userId);
            throw new RuntimeException("No company details found for the specified User ID");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create cell style for headers
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Create sheets for different types of data
            Sheet companySheet = workbook.createSheet("Company Details");
            Sheet panSheet = workbook.createSheet("PAN Details");
            Sheet chequeSheet = workbook.createSheet("Cheque Details");
            Sheet coiSheet = workbook.createSheet("COI Details");

            // Create headers
            createHeaders(companySheet, panSheet, chequeSheet, coiSheet, headerStyle);

            // Populate data for each company
            int companyRowNum = 1;
            int panRowNum = 1;
            int chequeRowNum = 1;
            int coiRowNum = 1;

            for (CompanyDetails company : companyDetailsList) {
                populateCompanyRow(companySheet.createRow(companyRowNum++), company);
                
                if (company.getPanDetails() != null) {
                    populatePanRow(panSheet.createRow(panRowNum++), company.getPanDetails());
                }
                
                if (company.getChequeDetails() != null) {
                    populateChequeRow(chequeSheet.createRow(chequeRowNum++), company.getChequeDetails());
                }
                
                if (company.getCertificateOfIncorporation() != null) {
                    populateCoiRow(coiSheet.createRow(coiRowNum++), company.getCertificateOfIncorporation());
                }
            }

            // Auto-size columns for all sheets
            autoSizeColumns(companySheet);
            autoSizeColumns(panSheet);
            autoSizeColumns(chequeSheet);
            autoSizeColumns(coiSheet);

            // Write to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating Excel report for userId: {}", userId, e);
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage());
        }
    }

    private void createHeaders(Sheet companySheet, Sheet panSheet, Sheet chequeSheet, Sheet coiSheet, CellStyle headerStyle) {
        // Company sheet headers
        Row companyHeader = companySheet.createRow(0);
        createHeaderCell(companyHeader, 0, "Company ID", headerStyle);
        createHeaderCell(companyHeader, 1, "GSTIN Number", headerStyle);
        createHeaderCell(companyHeader, 2, "Legal Trade Name", headerStyle);
        createHeaderCell(companyHeader, 3, "Registration Date", headerStyle);
        createHeaderCell(companyHeader, 4, "PAN/TIN/CST", headerStyle);
        createHeaderCell(companyHeader, 5, "Registration Type", headerStyle);
        createHeaderCell(companyHeader, 6, "Registered Address", headerStyle);
        createHeaderCell(companyHeader, 7, "Company Name", headerStyle);
        createHeaderCell(companyHeader, 8, "PAN Number", headerStyle);

        // PAN sheet headers
        Row panHeader = panSheet.createRow(0);
        createHeaderCell(panHeader, 0, "PAN ID", headerStyle);
        createHeaderCell(panHeader, 1, "PAN Number", headerStyle);
        createHeaderCell(panHeader, 2, "Name", headerStyle);
        createHeaderCell(panHeader, 3, "Date of Birth/Incorporation", headerStyle);
        createHeaderCell(panHeader, 4, "Father's Name", headerStyle);
        createHeaderCell(panHeader, 5, "Category", headerStyle);

        // Cheque sheet headers
        Row chequeHeader = chequeSheet.createRow(0);
        createHeaderCell(chequeHeader, 0, "Cheque ID", headerStyle);
        createHeaderCell(chequeHeader, 1, "Bank", headerStyle);
        createHeaderCell(chequeHeader, 2, "Code", headerStyle);
        createHeaderCell(chequeHeader, 3, "Issued To", headerStyle);
        createHeaderCell(chequeHeader, 4, "Signatory", headerStyle);
        createHeaderCell(chequeHeader, 5, "Account Number", headerStyle);
        createHeaderCell(chequeHeader, 6, "IFSC", headerStyle);
        createHeaderCell(chequeHeader, 7, "Issue Date", headerStyle);
        createHeaderCell(chequeHeader, 8, "Branch", headerStyle);

        // COI sheet headers
        Row coiHeader = coiSheet.createRow(0);
        createHeaderCell(coiHeader, 0, "COI ID", headerStyle);
        createHeaderCell(coiHeader, 1, "CIN Number", headerStyle);
        createHeaderCell(coiHeader, 2, "Created Date", headerStyle);
        createHeaderCell(coiHeader, 3, "Modified Date", headerStyle);
    }

    private void populateCompanyRow(Row row, CompanyDetails company) {
        row.createCell(0).setCellValue(company.getCompanyId());
        row.createCell(1).setCellValue(company.getGstinNumber());
        row.createCell(2).setCellValue(company.getLegalTradeName());
        row.createCell(3).setCellValue(company.getDateOfRegistration());
        row.createCell(4).setCellValue(company.getPanTinCst());
        row.createCell(5).setCellValue(company.getTypeOfRegistration());
        row.createCell(6).setCellValue(company.getRegisteredAddress());
        row.createCell(7).setCellValue(company.getCompanyName());
        row.createCell(8).setCellValue(company.getPanNumber());
    }

    private void populatePanRow(Row row, PanDetails details) {
        row.createCell(0).setCellValue(details.getPanDetailsId());
        row.createCell(1).setCellValue(details.getPanNumber());
        row.createCell(2).setCellValue(details.getName());
        row.createCell(3).setCellValue(details.getDateOfBirthIncorporation());
        row.createCell(4).setCellValue(details.getFathersName());
        row.createCell(5).setCellValue(details.getCategory());
    }

    private void populateChequeRow(Row row, ChequeDetails details) {
        row.createCell(0).setCellValue(details.getChequeDetailsId());
        row.createCell(1).setCellValue(details.getBank());
        row.createCell(2).setCellValue(details.getCode());
        row.createCell(3).setCellValue(details.getIssuedTo());
        row.createCell(4).setCellValue(details.getSignatory());
        row.createCell(5).setCellValue(details.getAccountNumber());
        row.createCell(6).setCellValue(details.getIfsc());
        row.createCell(7).setCellValue(details.getIssued());
        row.createCell(8).setCellValue(details.getBranch());
    }

    private void populateCoiRow(Row row, CertificateOfIncorporation details) {
        row.createCell(0).setCellValue(details.getCertificateOfIncorporationId());
        row.createCell(1).setCellValue(details.getCinNumber());
        row.createCell(2).setCellValue(details.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        row.createCell(3).setCellValue(details.getModifiedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    private void createHeaderCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet) {
        if (sheet.getPhysicalNumberOfRows() > 0) {
            Row row = sheet.getRow(0);
            for (int i = 0; i < row.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }
} 