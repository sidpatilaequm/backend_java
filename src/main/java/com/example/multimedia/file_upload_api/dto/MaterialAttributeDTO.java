package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class MaterialAttributeDTO {
    private String attributeName;
    private String attributeValue;
    private String type; // GENERAL or VARIANT

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialAttributeDTO that = (MaterialAttributeDTO) o;
        return java.util.Objects.equals(attributeName, that.attributeName)
                && java.util.Objects.equals(attributeValue, that.attributeValue)
                && java.util.Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(attributeName, attributeValue, type);
    }
} 