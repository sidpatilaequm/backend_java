package com.example.multimedia.file_upload_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

/** The buyer/category team under one {@link PurchasingOrg} (e.g. Raw Materials, Packaging, MRO). */
@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "purchasing_group")
public class PurchasingGroup {

    @Id
    @Column(name = "purch_group_code", length = 3)
    private String purchGroupCode;

    @Column(name = "purch_group_name", nullable = false, length = 100)
    private String purchGroupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purch_org_code", nullable = false)
    private PurchasingOrg purchasingOrg;
}
