package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.MasterBomResponseDto;
import com.example.multimedia.file_upload_api.entity.MasterBomFile;
import com.example.multimedia.file_upload_api.entity.MasterBomRecord;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.MasterBomFileRepository;
import com.example.multimedia.file_upload_api.repository.MasterBomRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasterBomService {

    private final MasterBomFileRepository fileRepository;
    private final MasterBomRecordRepository recordRepository;

    @Transactional
    public void uploadMasterBomExcel(MultipartFile file, SuperAdmin superAdmin) {
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {

            // Invalidate old files for this super admin
            fileRepository.invalidateAllActiveFiles(superAdmin);

            // Determine recent version for this super admin
            int nextVersion = 1;
            Optional<MasterBomFile> lastFile = fileRepository.findTopBySuperAdminOrderByIdDesc(superAdmin);
            if (lastFile.isPresent()) {
                nextVersion = lastFile.get().getVersion() + 1;
            }

            MasterBomFile newFile = MasterBomFile.builder()
                    .fileName(file.getOriginalFilename())
                    .uploadedBy(superAdmin.getEmail())
                    .superAdmin(superAdmin)
                    .isActive(true)
                    .version(nextVersion)
                    .build();

            newFile = fileRepository.save(newFile);

            Sheet sheet = workbook.getSheetAt(0);

            // Identify Header mappings
            Row headerRow = sheet.getRow(0);
            if (headerRow == null)
                throw new RuntimeException("Excel file is empty or missing header");

            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell.getCellType() == CellType.STRING) {
                    colMap.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex());
                }
            }

            int fgCol = findCol(colMap, "fg", "part number", "finished");
            int fgDescCol = findCol(colMap, "description", "fg desc");
            int rmCol = findCol(colMap, "rm", "raw", "item code");
            int rmDescCol = findCol(colMap, "rm desc", "raw desc");
            int qtyCol = findCol(colMap, "qty", "quantity");
            int uomCol = findCol(colMap, "um", "uom", "unit");
            int levelCol = findCol(colMap, "level");

            // Validate required columns
            if (fgCol == -1)
                throw new RuntimeException("Required column 'Part Number' or 'FG' not found in Excel");
            if (rmCol == -1)
                throw new RuntimeException("Required column 'Item Code' or 'RM' not found in Excel");
            if (qtyCol == -1)
                throw new RuntimeException("Required column 'Quantity' or 'QTY' not found in Excel");

            List<MasterBomRecord> records = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                String fgNumber = fgCol != -1 ? getCellVal(row.getCell(fgCol)) : null;
                if (fgNumber == null || fgNumber.isEmpty() || fgNumber.equalsIgnoreCase("null"))
                    continue;

                String fgDesc = fgDescCol != -1 ? getCellVal(row.getCell(fgDescCol)) : null;
                String rmItemCode = rmCol != -1 ? getCellVal(row.getCell(rmCol)) : null;
                String rmDesc = rmDescCol != -1 ? getCellVal(row.getCell(rmDescCol)) : null;
                String qtyStr = qtyCol != -1 ? getCellVal(row.getCell(qtyCol)) : null;
                String uom = uomCol != -1 ? getCellVal(row.getCell(uomCol)) : null;
                String levelStr = levelCol != -1 ? getCellVal(row.getCell(levelCol)) : null;

                Double qty = 0.0;
                try {
                    if (qtyStr != null && !qtyStr.isEmpty())
                        qty = Double.parseDouble(qtyStr);
                } catch (NumberFormatException ignored) {
                }

                Integer level = 1;
                try {
                    if (levelStr != null && !levelStr.isEmpty())
                        level = (int) Double.parseDouble(levelStr);
                } catch (NumberFormatException ignored) {
                }

                if (rmItemCode != null && !rmItemCode.isEmpty() && !rmItemCode.equalsIgnoreCase("null")) {
                    MasterBomRecord record = MasterBomRecord.builder()
                            .masterFile(newFile)
                            .fgNumber(fgNumber)
                            .description(fgDesc)
                            .rmItemCode(rmItemCode)
                            .rmDescription(rmDesc)
                            .qty(qty)
                            .uom(uom)
                            .level(level)
                            .build();
                    records.add(record);
                }
            }

            // Batch save
            recordRepository.saveAll(records);
            log.info("Saved {} records from uploaded BOM", records.size());

        } catch (Exception e) {
            log.error("Failed to parse Master BOM Excel", e);
            throw new RuntimeException("Failed to upload Excel: " + e.getMessage());
        }
    }

    public MasterBomResponseDto fetchBomForFgNumber(String fgNumber, SuperAdmin superAdmin, Optional<Long> fileId) {
        MasterBomFile targetFile;
        if (fileId.isPresent()) {
            targetFile = fileRepository.findById(fileId.get())
                    .orElseThrow(() -> new RuntimeException("Specified Master BOM file not found: " + fileId.get()));
        } else {
            targetFile = fileRepository.findTopByIsActiveAndSuperAdminOrderByIdDesc(true, superAdmin)
                    .orElseThrow(
                            () -> new RuntimeException("No active Master BOM file found. Please upload one first."));
        }

        // Refined Search Logic:
        // 1. Identify base part number (e.g., "102122-RevB" -> "102122")
        String basePart = fgNumber.split("[ \\-_]")[0];

        // 2. Fetch all possible matches by base prefix from DB
        List<MasterBomRecord> allRelatedRecords = recordRepository.findByMasterFileAndFgNumberStartingWith(targetFile,
                basePart);

        if (allRelatedRecords.isEmpty()) {
            return MasterBomResponseDto.builder()
                    .status("error")
                    .data(null)
                    .build();
        }

        // 3. Try to filter for the specific input (normalized comparison)
        String normalizedInput = fgNumber.replace(" ", "").replace("-", "").replace("_", "").toLowerCase();

        List<MasterBomRecord> filteredRecords = allRelatedRecords.stream()
                .filter(r -> r.getFgNumber().replace(" ", "").replace("-", "").replace("_", "").toLowerCase()
                        .equals(normalizedInput))
                .collect(Collectors.toList());

        List<MasterBomRecord> finalRecords;
        if (!filteredRecords.isEmpty()) {
            finalRecords = filteredRecords;
        } else {
            // 4. Fallback: Show all records found under the base part number
            log.info("No specific match for '{}'. Returning all records for base part '{}'.", fgNumber, basePart);
            finalRecords = allRelatedRecords;
        }

        MasterBomRecord first = finalRecords.get(0);

        MasterBomResponseDto.HeaderData headerData = MasterBomResponseDto.HeaderData.builder()
                .partNumber(first.getFgNumber())
                .description(first.getDescription())
                .source("MASTER_EXCEL_DB")
                .build();

        List<MasterBomResponseDto.BomItem> bomItems = finalRecords.stream()
                .map(r -> MasterBomResponseDto.BomItem.builder()
                        .itemCode(r.getRmItemCode())
                        .description(r.getRmDescription())
                        .qty(r.getQty())
                        .uom(r.getUom())
                        .level(r.getLevel())
                        .procurementType("M")
                        .build())
                .collect(Collectors.toList());

        MasterBomResponseDto.Data data = MasterBomResponseDto.Data.builder()
                .headerData(headerData)
                .bom(bomItems)
                .build();

        return MasterBomResponseDto.builder()
                .status("success")
                .data(data)
                .build();
    }

    public List<MasterBomFile> getAllMasterBomFiles(SuperAdmin superAdmin) {
        return fileRepository.findAllBySuperAdminOrderByIdDesc(superAdmin);
    }

    private int findCol(Map<String, Integer> colMap, String... keywords) {
        for (String k : keywords) {
            for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
                if (entry.getKey().contains(k))
                    return entry.getValue();
            }
        }
        return -1; // Not found
    }

    private String getCellVal(Cell cell) {
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    double val = cell.getNumericCellValue();
                    if (val == (long) val)
                        yield String.valueOf((long) val);
                    yield String.valueOf(val);
                }
            }
            default -> "";
        };
    }
}
