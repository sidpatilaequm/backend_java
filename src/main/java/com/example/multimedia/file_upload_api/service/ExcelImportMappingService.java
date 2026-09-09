package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ExcelColumnMappingDto;
import com.example.multimedia.file_upload_api.dto.ExcelImportMappingDto;
import com.example.multimedia.file_upload_api.dto.ExcelInspectResultDto;
import com.example.multimedia.file_upload_api.dto.ExcelTargetColumnDto;
import com.example.multimedia.file_upload_api.entity.ExcelImportMapping;
import com.example.multimedia.file_upload_api.enums.ExcelReportType;
import com.example.multimedia.file_upload_api.repository.ExcelImportMappingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Backs the admin "which Excel column feeds which DB column" screen for the SAP report excels
 * and ASN (ExcelReportType). Saves/reads the mapping, lets the admin inspect an uploaded sample
 * file's headers and the live target table's columns, and (exportGenericToExcel) generates a
 * real download for the single-table report types using that same saved mapping in reverse.
 * FolderIt watching/cron import is a later phase -- not built yet.
 */
@Service
public class ExcelImportMappingService {

    private final ExcelImportMappingRepository repository;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExcelImportMappingService(ExcelImportMappingRepository repository, DataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    public List<ExcelImportMappingDto> listAll() {
        List<ExcelImportMappingDto> out = new ArrayList<>();
        for (ExcelReportType type : ExcelReportType.values()) {
            out.add(toDto(type, repository.findByReportType(type).orElse(null)));
        }
        return out;
    }

    public ExcelImportMappingDto get(ExcelReportType type) {
        return toDto(type, repository.findByReportType(type).orElse(null));
    }

    public ExcelImportMappingDto save(ExcelReportType type, ExcelImportMappingDto dto, Long adminUserId) {
        ExcelImportMapping entity = repository.findByReportType(type).orElseGet(() -> {
            ExcelImportMapping e = new ExcelImportMapping();
            e.setReportType(type);
            return e;
        });
        entity.setSheetName(dto.getSheetName());
        entity.setHeaderRow(dto.getHeaderRow() != null ? dto.getHeaderRow() : 5);
        entity.setMappingJson(writeMappingJson(dto.getMapping()));
        entity.setUpdatedBy(adminUserId);
        return toDto(type, repository.save(entity));
    }

    /** Live columns of the report's target table (+ item table, if it has one), straight from
     *  information_schema -- never goes stale. */
    public List<ExcelTargetColumnDto> targetColumns(ExcelReportType type) {
        List<ExcelTargetColumnDto> out = new ArrayList<>(tableColumns(type.getTargetTable(), "header"));
        if (type.getItemTable() != null) {
            out.addAll(tableColumns(type.getItemTable(), "item"));
        }
        return out;
    }

    private List<ExcelTargetColumnDto> tableColumns(String table, String tableRole) {
        List<ExcelTargetColumnDto> out = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            // Primary keys are deliberately left mappable -- e.g. a "Sr. No." column can be a
            // legitimate source for id. Only FK relationship columns and the two timestamp
            // columns are actually system-set (Hibernate/DB fill those in, an excel value would
            // just be ignored or fought over), so only those stay locked.
            Set<String> foreignKeys = new HashSet<>();
            try (ResultSet rs = meta.getImportedKeys(conn.getCatalog(), null, table)) {
                while (rs.next()) foreignKeys.add(rs.getString("FKCOLUMN_NAME"));
            }

            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table, null)) {
                while (rs.next()) {
                    String name = rs.getString("COLUMN_NAME");
                    String colType = rs.getString("TYPE_NAME");
                    boolean nullable = "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"));

                    String reason = null;
                    if (foreignKeys.contains(name)) reason = "foreign key";
                    else if (name.equals("created_at")) reason = "set automatically when the row is created";
                    else if (name.equals("updated_at")) reason = "set automatically whenever the row changes";

                    out.add(new ExcelTargetColumnDto(name, colType, nullable, reason != null, reason, tableRole));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not read columns of " + table + ": " + e.getMessage(), e);
        }
        return out;
    }

    /** For the single-table report types: SELECT the mapped columns straight from the target
     *  table and write a normal spreadsheet (header row + one row per DB row). Report types
     *  with an item table (INVOICES, ASN) aren't exported here -- ASN goes through
     *  AsnController (it needs vendor-scoped visibility); INVOICES has no live export need yet. */
    public byte[] exportGenericToExcel(ExcelReportType type) {
        if (type.getItemTable() != null) {
            throw new IllegalArgumentException(type + " has an item table and isn't exported generically");
        }
        ExcelImportMapping saved = repository.findByReportType(type)
                .orElseThrow(() -> new IllegalStateException("No mapping saved for " + type + " yet"));
        List<ExcelColumnMappingDto> mapping = readMappingJson(saved.getMappingJson());
        if (mapping.isEmpty()) {
            throw new IllegalStateException("Mapping for " + type + " has no columns mapped yet");
        }

        String columnList = mapping.stream().map(ExcelColumnMappingDto::getDbColumn)
                .reduce((a, b) -> a + ", " + b).orElse("");
        String sql = "SELECT " + columnList + " FROM " + type.getTargetTable();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(type.getLabel());
            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < mapping.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(mapping.get(i).getExcelColumn());
                cell.setCellStyle(headerStyle);
            }

            try (Connection conn = dataSource.getConnection();
                 java.sql.Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                int r = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(r++);
                    for (int i = 0; i < mapping.size(); i++) {
                        Object value = rs.getObject(i + 1);
                        writeCell(row.createCell(i), value);
                    }
                }
            }

            for (int i = 0; i < mapping.size(); i++) sheet.autoSizeColumn(i);

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Could not build export for " + type + ": " + e.getMessage(), e);
        }
    }

    /** Shared by exportGenericToExcel and (via AsnController) the ASN export. */
    public static void writeCell(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.sql.Date || value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
        } else if (value instanceof java.time.LocalDate) {
            cell.setCellValue((java.time.LocalDate) value);
        } else if (value instanceof java.time.LocalDateTime) {
            cell.setCellValue((java.time.LocalDateTime) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /** Sheet names, and the header row's column names, of an uploaded sample workbook. */
    public ExcelInspectResultDto inspect(MultipartFile file, String requestedSheet, int headerRow) {
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            List<String> sheetNames = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) sheetNames.add(workbook.getSheetName(i));

            Sheet sheet = (requestedSheet != null && workbook.getSheet(requestedSheet) != null)
                    ? workbook.getSheet(requestedSheet) : workbook.getSheetAt(0);

            DataFormatter formatter = new DataFormatter();
            List<String> headers = new ArrayList<>();
            Row headRow = sheet.getRow(headerRow - 1);
            if (headRow != null) {
                for (Cell cell : headRow) {
                    String v = formatter.formatCellValue(cell).trim();
                    if (!v.isEmpty()) headers.add(v);
                }
            }

            Map<String, String> sampleRow = new LinkedHashMap<>();
            Row firstDataRow = sheet.getRow(headerRow);
            if (firstDataRow != null) {
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = firstDataRow.getCell(i);
                    sampleRow.put(headers.get(i), cell == null ? "" : formatter.formatCellValue(cell));
                }
            }

            ExcelInspectResultDto out = new ExcelInspectResultDto();
            out.setSheets(sheetNames);
            out.setSheet(sheet.getSheetName());
            out.setHeaders(headers);
            out.setSampleRow(sampleRow);
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Could not read the uploaded file: " + e.getMessage(), e);
        }
    }

    private ExcelImportMappingDto toDto(ExcelReportType type, ExcelImportMapping entity) {
        ExcelImportMappingDto dto = new ExcelImportMappingDto();
        dto.setReportType(type);
        dto.setLabel(type.getLabel());
        dto.setTargetTable(type.getTargetTable());
        if (entity == null) {
            dto.setSheetName(null);
            dto.setHeaderRow(5);
            dto.setMapping(new ArrayList<>());
            dto.setConfigured(false);
        } else {
            dto.setSheetName(entity.getSheetName());
            dto.setHeaderRow(entity.getHeaderRow());
            dto.setMapping(readMappingJson(entity.getMappingJson()));
            dto.setConfigured(!dto.getMapping().isEmpty());
            dto.setUpdatedAt(entity.getUpdatedAt());
        }
        return dto;
    }

    private String writeMappingJson(List<ExcelColumnMappingDto> mapping) {
        try {
            return objectMapper.writeValueAsString(mapping != null ? mapping : new ArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize mapping: " + e.getMessage(), e);
        }
    }

    private List<ExcelColumnMappingDto> readMappingJson(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<ExcelColumnMappingDto>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
