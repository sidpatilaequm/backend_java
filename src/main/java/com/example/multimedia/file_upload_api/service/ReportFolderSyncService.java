package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ExcelColumnMappingDto;
import com.example.multimedia.file_upload_api.entity.ExcelImportMapping;
import com.example.multimedia.file_upload_api.entity.ReportFolderSync;
import com.example.multimedia.file_upload_api.entity.ReportFolderSyncFile;
import com.example.multimedia.file_upload_api.enums.ExcelReportType;
import com.example.multimedia.file_upload_api.repository.ExcelImportMappingRepository;
import com.example.multimedia.file_upload_api.repository.ReportFolderSyncFileRepository;
import com.example.multimedia.file_upload_api.repository.ReportFolderSyncRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * FolderIt watcher for the excel-to-table report types: every minute, checks whichever
 * report_folder_sync rows are enabled and due (own interval_minutes), lists that report's
 * FolderIt folder, and imports any excel file that's new or has a newer FolderIt updatedAt than
 * the last time it was processed (services/report_folder_sync_file) -- so both a brand-new file
 * and a re-uploaded/edited one get picked up.
 *
 * Rows are upserted against a natural key (see NATURAL_KEYS below) so a changed row in an
 * already-imported file updates the matching DB row instead of duplicating it -- matched at the
 * application level (SELECT then UPDATE/INSERT), not a DB unique constraint, since this only
 * ever runs from this one scheduled job, never concurrently.
 *
 * INVOICES is deliberately not included yet -- vendor_invoice's vendor_company_id/vendor_user_id
 * are NOT NULL FKs designed for a real portal-submitted invoice, not a bulk SAP import, and need
 * more design work before rows can be inserted there safely.
 *
 * This is a separate, dedicated job from FolderitBatchJobService (Master PO sync) -- deliberately
 * not touching that file, which a colleague is actively working on.
 */
@Service
public class ReportFolderSyncService {

    private static final Logger logger = LoggerFactory.getLogger(ReportFolderSyncService.class);

    /** DB columns (post-rename, matching the excel headers) that identify "the same row" on a
     *  re-import, per report type -- confirmed with the user before building this. */
    private static final Map<ExcelReportType, List<String>> NATURAL_KEYS = Map.of(
        ExcelReportType.VENDOR_PAYMENTS, List.of("invoice_no", "payment_document_no"),
        ExcelReportType.VENDOR_RETURNS, List.of("return_document_no", "po_line_item"),
        ExcelReportType.CREDIT_NOTES, List.of("credit_note_no", "po_line_item"),
        ExcelReportType.VENDOR_STOCK, List.of("vendor_no", "material_code")
    );

    private static final Set<String> EXCEL_EXTENSIONS = Set.of(".xlsx", ".xls", ".csv");

    private final ReportFolderSyncRepository syncRepository;
    private final ReportFolderSyncFileRepository fileRepository;
    private final ExcelImportMappingRepository mappingRepository;
    private final FolderItService folderItService;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportFolderSyncService(ReportFolderSyncRepository syncRepository,
                                    ReportFolderSyncFileRepository fileRepository,
                                    ExcelImportMappingRepository mappingRepository,
                                    FolderItService folderItService,
                                    DataSource dataSource) {
        this.syncRepository = syncRepository;
        this.fileRepository = fileRepository;
        this.mappingRepository = mappingRepository;
        this.folderItService = folderItService;
        this.dataSource = dataSource;
    }

    // Checks every minute which report(s) are actually due, per their own interval_minutes.
    @Scheduled(fixedDelay = 60000)
    public void scheduleTask() {
        List<ReportFolderSync> configs = syncRepository.findByEnabledTrue();
        for (ReportFolderSync config : configs) {
            if (config.getLastRunAt() != null) {
                LocalDateTime nextRun = config.getLastRunAt().plusMinutes(config.getIntervalMinutes());
                if (LocalDateTime.now().isBefore(nextRun)) continue;
            }
            try {
                runSync(config);
            } catch (Exception e) {
                logger.error("FolderIt sync failed for {}: {}", config.getReportType(), e.getMessage(), e);
                config.setLastRunStatus("Error: " + e.getMessage());
                config.setLastRunAt(LocalDateTime.now());
                syncRepository.save(config);
            }
        }
    }

    public synchronized void runSync(ReportFolderSync config) throws Exception {
        ExcelReportType type = ExcelReportType.valueOf(config.getReportType());
        ExcelImportMapping mappingRow = mappingRepository.findByReportType(type)
                .orElse(null);
        if (mappingRow == null || mappingRow.getMappingJson() == null) {
            config.setLastRunStatus("No mapping configured for " + type + " yet");
            config.setLastRunAt(LocalDateTime.now());
            syncRepository.save(config);
            return;
        }
        List<ExcelColumnMappingDto> mapping = objectMapper.readValue(
                mappingRow.getMappingJson(), new TypeReference<List<ExcelColumnMappingDto>>() {});
        if (mapping.isEmpty()) {
            config.setLastRunStatus("Mapping for " + type + " has no columns mapped yet");
            config.setLastRunAt(LocalDateTime.now());
            syncRepository.save(config);
            return;
        }

        JSONArray entries = folderItService.getFilesInFolder(config.getFolderitFolderUid());
        int filesProcessed = 0;
        StringBuilder status = new StringBuilder();

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            if (!"file".equalsIgnoreCase(entry.optString("type", ""))) continue;
            String name = entry.optString("name", "");
            String lower = name.toLowerCase();
            boolean isExcel = EXCEL_EXTENSIONS.stream().anyMatch(lower::endsWith);
            if (!isExcel) continue;

            String fileUid = entry.getString("uid");
            long updatedAt = entry.optLong("updatedAt", 0);

            ReportFolderSyncFile seen = fileRepository.findByReportTypeAndFileUid(config.getReportType(), fileUid)
                    .orElse(null);
            if (seen != null && seen.getLastProcessedUpdatedAt() != null && seen.getLastProcessedUpdatedAt() >= updatedAt) {
                continue; // already processed this exact version
            }

            if (seen == null) {
                seen = new ReportFolderSyncFile();
                seen.setReportType(config.getReportType());
                seen.setFileUid(fileUid);
            }
            seen.setFileName(name);

            try {
                FolderItService.DownloadedFile downloaded = folderItService.downloadFileBytes(fileUid);
                int rows = importRows(type, mappingRow, mapping, downloaded.bytes());
                seen.setRowsProcessed(rows);
                seen.setLastError(null);
                seen.setLastProcessedUpdatedAt(updatedAt);
                seen.setLastProcessedAt(LocalDateTime.now());
                filesProcessed++;
                status.append(name).append(": ").append(rows).append(" rows; ");
                logger.info("FolderIt sync [{}]: imported {} rows from {}", type, rows, name);
            } catch (Exception e) {
                seen.setLastError(e.getMessage());
                status.append(name).append(": FAILED (").append(e.getMessage()).append("); ");
                logger.error("FolderIt sync [{}]: failed to import {}: {}", type, name, e.getMessage(), e);
            }
            fileRepository.save(seen);
        }

        config.setLastRunAt(LocalDateTime.now());
        config.setFilesProcessedLastRun(filesProcessed);
        config.setLastRunStatus(status.length() == 0 ? "No new/changed files" : status.toString().trim());
        syncRepository.save(config);
    }

    /** Reads the excel per the saved mapping, upserts each row, returns the row count processed. */
    private int importRows(ExcelReportType type, ExcelImportMapping mappingRow,
                            List<ExcelColumnMappingDto> mapping, byte[] fileBytes) throws Exception {
        List<String> naturalKey = NATURAL_KEYS.get(type);
        if (naturalKey == null) {
            throw new IllegalStateException(type + " has no natural key configured -- not supported for FolderIt sync yet");
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes); Workbook workbook = WorkbookFactory.create(is)) {
            String sheetName = mappingRow.getSheetName();
            Sheet sheet = (sheetName != null && workbook.getSheet(sheetName) != null)
                    ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);

            int headerRowIdx = (mappingRow.getHeaderRow() != null ? mappingRow.getHeaderRow() : 1) - 1;
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(headerRowIdx);
            if (headerRow == null) return 0;

            // excel header text -> column index
            Map<String, Integer> headerIndex = new HashMap<>();
            for (Cell cell : headerRow) {
                String v = formatter.formatCellValue(cell).trim();
                if (!v.isEmpty()) headerIndex.put(v, cell.getColumnIndex());
            }

            int processed = 0;
            int r = headerRowIdx + 1;
            while (true) {
                Row row = sheet.getRow(r);
                if (row == null || isRowBlank(row)) break;

                Map<String, Object> values = new LinkedHashMap<>();
                for (ExcelColumnMappingDto entry : mapping) {
                    Integer colIdx = headerIndex.get(entry.getExcelColumn());
                    if (colIdx == null) continue;
                    Cell cell = row.getCell(colIdx);
                    values.put(entry.getDbColumn(), cellValue(cell));
                }

                boolean hasKey = naturalKey.stream().allMatch(k -> {
                    Object v = values.get(k);
                    return v != null && !v.toString().isBlank();
                });
                if (hasKey) {
                    upsertRow(type.getTargetTable(), naturalKey, values);
                    processed++;
                } else {
                    logger.warn("FolderIt sync [{}]: skipping row {} -- missing natural key value", type, r + 1);
                }
                r++;
            }
            return processed;
        }
    }

    private boolean isRowBlank(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !formatCellSafe(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String formatCellSafe(Cell cell) {
        try {
            return new DataFormatter().formatCellValue(cell);
        } catch (Exception e) {
            return "";
        }
    }

    private Object cellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }
                return java.math.BigDecimal.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return java.math.BigDecimal.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return null;
            default:
                String s = cell.getStringCellValue();
                return (s == null || s.isBlank()) ? null : s.trim();
        }
    }

    private void upsertRow(String table, List<String> naturalKey, Map<String, Object> values) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            String whereClause = String.join(" AND ", naturalKey.stream().map(k -> k + " = ?").toList());
            String findSql = "SELECT id FROM " + table + " WHERE " + whereClause;
            Long existingId = null;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                int i = 1;
                for (String k : naturalKey) ps.setObject(i++, values.get(k));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) existingId = rs.getLong("id");
                }
            }

            List<String> columns = new ArrayList<>(values.keySet());
            if (existingId != null) {
                String setClause = String.join(", ", columns.stream().map(c -> c + " = ?").toList());
                String updateSql = "UPDATE " + table + " SET " + setClause + " WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    int i = 1;
                    for (String c : columns) ps.setObject(i++, values.get(c));
                    ps.setLong(i, existingId);
                    ps.executeUpdate();
                }
            } else {
                String colList = String.join(", ", columns);
                String placeholders = String.join(", ", columns.stream().map(c -> "?").toList());
                String insertSql = "INSERT INTO " + table + " (" + colList + ") VALUES (" + placeholders + ")";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    int i = 1;
                    for (String c : columns) ps.setObject(i++, values.get(c));
                    ps.executeUpdate();
                }
            }
        }
    }
}
