package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private Long countryId;

    @NotBlank(message = "Country name is required")
    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "iso_code")
    private String isoCode;

    @Column(name = "phone_code")
    private String phoneCode;

    @Column(name = "status")
    private String status; // ACTIVE / INACTIVE

    @ManyToOne
    @JoinColumn(name = "super_admin_id")
    private SuperAdmin superAdmin;
}
