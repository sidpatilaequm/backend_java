package com.example.multimedia.file_upload_api.enums;

public enum BaseUnit {
    // Weight Units
    KG("KG", "Kilograms"),
    G("G", "Grams"),
   
    
    // Length Units
    M("M", "Meters"),
    FT("FT", "Feet"),
    
    
    
    // Volume Units
    LITERS("LITERS", "Liters"),
    ML("ML", "Milliliters"),
    

    PIECES("PIECES", "Pieces"),
    PAIR("PAIR", "Pair"),
    SET("SET", "Set");

    private final String code;
    private final String description;

    BaseUnit(String code, String description) {
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
        return code + " - " + description;
    }
} 