package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * SAP-style "company code" — our own buying organisation's legal entity, the top of the
 * plant / purchasing-org / purchasing-group hierarchy (enterprise structure master data).
 *
 * Not to be confused with {@link CompanyDetails}, which is a per-vendor profile row (one per
 * onboarded supplier account, keyed by company_id) — a completely different concept that
 * happens to share part of the name.
 */
@Data
@Entity
@Table(name = "company")
public class Company {

    @Id
    @Column(name = "company_code", length = 4)
    private String companyCode;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "gst_number", length = 15)
    private String gstNumber;
}
