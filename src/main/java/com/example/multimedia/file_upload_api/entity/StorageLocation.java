package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * A plant's inventory object (T001L) — holds stock, and may optionally be warehouse managed.
 * NOT the same thing as {@link PlantLocation} (the maintenance object): a plant location is
 * where equipment lives (Shop Floor, Utility); a storage location is where stock lives, and
 * only a storage location can carry a {@link Warehouse}.
 */
@Data
@Entity
@Table(name = "storage_location")
@IdClass(StorageLocation.Pk.class)
public class StorageLocation {

    @Id
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    @Id
    @Column(name = "sloc_id", length = 4)
    private String slocId;

    @Column(name = "description", nullable = false, length = 60)
    private String description;

    @Column(name = "is_warehouse_managed", nullable = false)
    private boolean warehouseManaged = false;

    @Data
    public static class Pk implements Serializable {
        private String plantCode;
        private String slocId;
    }
}
