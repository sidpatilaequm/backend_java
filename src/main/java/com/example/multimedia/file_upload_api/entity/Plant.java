package com.example.multimedia.file_upload_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

/**
 * A physical site (factory/warehouse) under one {@link Company}. Carries its own optional
 * gst_number rather than inheriting the company's, since a company registered in several
 * states holds a different GSTIN per state.
 */
@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "plant")
public class Plant {

    @Id
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    @Column(name = "plant_name", nullable = false, length = 100)
    private String plantName;

    @Column(name = "gst_number", length = 15)
    private String gstNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_code", nullable = false)
    private Company company;
}
