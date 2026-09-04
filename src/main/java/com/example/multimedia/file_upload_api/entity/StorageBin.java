package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * One physical storage bin within a warehouse. This is the one object in the enterprise
 * structure hierarchy that's actually operational data rather than a one-time master-data seed —
 * a warehouse can hold thousands of these, created by range rather than one at a time. Loading
 * them into SAP (LS05 / an LSMW load) is a separate step; bins here are this application's own
 * record only.
 */
@Data
@Entity
@Table(name = "storage_bin")
@IdClass(StorageBin.Pk.class)
public class StorageBin {

    @Id
    @Column(name = "warehouse_no", length = 3)
    private String warehouseNo;

    @Id
    @Column(name = "bin_code", length = 10)
    private String binCode;

    @Column(name = "storage_type", nullable = false, length = 3)
    private String storageType = "001";

    @Column(name = "storage_section", nullable = false, length = 3)
    private String storageSection = "001";

    @Column(name = "bin_type", length = 3)
    private String binType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Data
    public static class Pk implements Serializable {
        private String warehouseNo;
        private String binCode;
    }
}
