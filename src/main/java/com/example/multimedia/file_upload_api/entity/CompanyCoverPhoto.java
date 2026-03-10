package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_cover_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCoverPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cover_photo_id")
    private Long coverPhotoId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "cover_photo_name", nullable = false)
    private String coverPhotoName;

    @Column(name = "cover_photo_type", nullable = false)
    private String coverPhotoType; // e.g., "image/jpeg", "image/png"

    @Lob
    @Column(name = "cover_photo_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] coverPhotoData;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "sequence_order")
    private Integer sequenceOrder = 0;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;
}
