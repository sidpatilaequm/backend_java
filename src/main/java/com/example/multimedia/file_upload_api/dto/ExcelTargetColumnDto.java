package com.example.multimedia.file_upload_api.dto;

public class ExcelTargetColumnDto {
    private String name;
    private String type;
    private boolean nullable;

    public ExcelTargetColumnDto() {}

    public ExcelTargetColumnDto(String name, String type, boolean nullable) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }
}
