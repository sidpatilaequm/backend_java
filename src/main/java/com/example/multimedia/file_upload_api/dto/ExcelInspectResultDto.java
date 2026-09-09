package com.example.multimedia.file_upload_api.dto;

import java.util.List;
import java.util.Map;

public class ExcelInspectResultDto {
    private List<String> sheets;
    private String sheet;
    private List<String> headers;
    private Map<String, String> sampleRow;

    public List<String> getSheets() { return sheets; }
    public void setSheets(List<String> sheets) { this.sheets = sheets; }
    public String getSheet() { return sheet; }
    public void setSheet(String sheet) { this.sheet = sheet; }
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }
    public Map<String, String> getSampleRow() { return sampleRow; }
    public void setSampleRow(Map<String, String> sampleRow) { this.sampleRow = sampleRow; }
}
