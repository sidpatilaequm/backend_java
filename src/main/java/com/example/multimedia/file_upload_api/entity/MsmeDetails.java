package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_msme")
@Getter
@Setter
public class MsmeDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long msmeDetailsId;

    @OneToOne
    @JoinColumn(name = "company_id")
    private CompanyDetails company;

    private String udyamNumber;
    private String entityName;
    private String type;

    @Column(columnDefinition = "TEXT")
    private String majorActivity;

    private String gender;
    private String socialCategory;
    private String incorporatedDate;
    private String commencedDate;
    private String registeredDate;

    @Column(columnDefinition = "LONGTEXT")
    private String classifications; // JSON array of classifications

    @Column(columnDefinition = "LONGTEXT")
    private String locations; // JSON array of locations

    @Column(columnDefinition = "LONGTEXT")
    private String officialAddress; // JSON object of address

    @Column(columnDefinition = "LONGTEXT")
    private String nicCodes; // JSON array of NIC codes

    private String dic;
    private String dfo;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
