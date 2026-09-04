package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * A plant's maintenance object (T499S) — Shop Floor, Utility, Infrastructure, etc. Free-text key,
 * unique within its plant only. NOT a storage location — see StorageLocation's javadoc for why
 * these are modelled separately. Read-only in a real deployment (comes from ERP config); this
 * app lets an admin seed/maintain it directly, same as Plant itself.
 */
@Data
@Entity
@Table(name = "plant_location")
@IdClass(PlantLocation.Pk.class)
public class PlantLocation {

    @Id
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    @Id
    @Column(name = "location_id", length = 10)
    private String locationId;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Data
    public static class Pk implements Serializable {
        private String plantCode;
        private String locationId;
    }
}
