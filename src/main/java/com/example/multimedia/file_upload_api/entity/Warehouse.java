package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * A warehouse-managed storage location. The composite foreign key (plant_code, sloc_id, wh_flag)
 * references storage_location(plant_code, sloc_id, is_warehouse_managed) — wh_flag is a
 * database-generated constant 1, so this row simply cannot exist against a storage location
 * whose is_warehouse_managed isn't 1. Enforced by MySQL, not application code; wh_flag is never
 * set from Java (insertable/updatable = false, matching STORED GENERATED).
 */
@Data
@Entity
@Table(name = "warehouse")
public class Warehouse {

    @Id
    @Column(name = "warehouse_no", length = 3)
    private String warehouseNo;

    @Column(name = "description", nullable = false, length = 80)
    private String description;

    @Column(name = "plant_code", nullable = false, length = 4)
    private String plantCode;

    @Column(name = "sloc_id", nullable = false, length = 4)
    private String slocId;

    @Column(name = "wh_flag", insertable = false, updatable = false)
    private Integer whFlag;
}
