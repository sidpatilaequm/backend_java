package com.example.multimedia.file_upload_api.dto;

public class ExcelColumnMappingDto {
    private String excelColumn;
    private String dbColumn;

    public ExcelColumnMappingDto() {}

    public ExcelColumnMappingDto(String excelColumn, String dbColumn) {
        this.excelColumn = excelColumn;
        this.dbColumn = dbColumn;
    }

    public String getExcelColumn() { return excelColumn; }
    public void setExcelColumn(String excelColumn) { this.excelColumn = excelColumn; }
    public String getDbColumn() { return dbColumn; }
    public void setDbColumn(String dbColumn) { this.dbColumn = dbColumn; }
}
