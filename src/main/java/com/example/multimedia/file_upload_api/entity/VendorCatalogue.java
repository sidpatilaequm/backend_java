package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_catalogue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorCatalogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "catalogue_id")
    private Long catalogueId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType; // pdf, xlsx, csv

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // Size in bytes

    @Lob
    @Column(name = "file_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserDetail user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_id", nullable = false)
    private SuperAdmin superAdmin;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}
