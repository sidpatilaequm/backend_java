package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.enums.ExcelReportType;

import java.time.LocalDateTime;
import java.util.List;

public class ExcelImportMappingDto {
    private ExcelReportType reportType;
    private String label;
    private String targetTable;
    private String sheetName;
    private Integer headerRow;
    private List<ExcelColumnMappingDto> mapping;
    private boolean configured;
    private LocalDateTime updatedAt;

    public ExcelReportType getReportType() { return reportType; }
    public void setReportType(ExcelReportType reportType) { this.reportType = reportType; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }
    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public Integer getHeaderRow() { return headerRow; }
    public void setHeaderRow(Integer headerRow) { this.headerRow = headerRow; }
    public List<ExcelColumnMappingDto> getMapping() { return mapping; }
    public void setMapping(List<ExcelColumnMappingDto> mapping) { this.mapping = mapping; }
    public boolean isConfigured() { return configured; }
    public void setConfigured(boolean configured) { this.configured = configured; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
