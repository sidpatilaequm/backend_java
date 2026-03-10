package com.example.multimedia.file_upload_api.enums;

public enum MaterialType {
    ROH("ROH", "Raw Material"),
    HALB("HALB", "Semi-Finished Product"),
    FERT("FERT", "Finished Product"),
    HAWA("HAWA", "Trading Goods"),
    VERP("VERP", "Packaging Material"),
    NLAG("NLAG", "Non-Stock Material");

    private final String code;
    private final String description;

    MaterialType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayValue() {
        return code + "- " + description;
    }
} 