package com.example.multimedia.file_upload_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

/** A purchasing organisation under one {@link Company} — who has authority to issue POs on its behalf. */
@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "purchasing_org")
public class PurchasingOrg {

    @Id
    @Column(name = "purch_org_code", length = 4)
    private String purchOrgCode;

    @Column(name = "purch_org_name", nullable = false, length = 100)
    private String purchOrgName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_code", nullable = false)
    private Company company;
}
