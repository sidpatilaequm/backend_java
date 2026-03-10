package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class CertificateOfIncorporation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificateOfIncorporationId;
    
    @Column(name = "cin_number", columnDefinition = "VARCHAR(255)")
    private String cinNumber;

    @OneToOne
    @JoinColumn(name = "company_id")
    private CompanyDetails company;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}