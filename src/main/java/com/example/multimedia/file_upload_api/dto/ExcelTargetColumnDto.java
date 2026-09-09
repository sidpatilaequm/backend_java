package com.example.multimedia.file_upload_api.dto;

public class ExcelTargetColumnDto {
    private String name;
    private String type;
    private boolean nullable;
    /** Primary key, a foreign-key relationship column, or created_at/updated_at -- the UI shows
     *  these but doesn't offer them in the "map to an excel column" dropdown; the DB (or JPA)
     *  manages their value, not an excel import. */
    private boolean systemManaged;
    private String systemManagedReason;
    /** "header" or "item" -- which of the report type's tables this column came from (most
     *  report types have only a header table, so this is "header" for everything they have;
     *  INVOICES and ASN also have an item table). Display/grouping only. */
    private String table;

    public ExcelTargetColumnDto() {}

    public ExcelTargetColumnDto(String name, String type, boolean nullable, boolean systemManaged, String systemManagedReason, String table) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.systemManaged = systemManaged;
        this.systemManagedReason = systemManagedReason;
        this.table = table;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }
    public boolean isSystemManaged() { return systemManaged; }
    public void setSystemManaged(boolean systemManaged) { this.systemManaged = systemManaged; }
    public String getSystemManagedReason() { return systemManagedReason; }
    public void setSystemManagedReason(String systemManagedReason) { this.systemManagedReason = systemManagedReason; }
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
}
