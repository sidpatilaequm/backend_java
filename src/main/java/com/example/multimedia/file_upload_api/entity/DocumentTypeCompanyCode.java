package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Which company codes a {@link DocumentType} may actually be granted in — a type is only usable
 * where it has an assignment row here. Plain scalar columns (not @ManyToOne relations) by
 * design: {@link PurchaseRoleGrant}'s two composite foreign keys reference this table's PK
 * directly at the database level (see the migration SQL) — Hibernate only needs to read/write
 * these as ordinary columns, not model the relational graph itself.
 */
@Data
@NoArgsConstructor
@Entity
@IdClass(DocumentTypeCompanyCode.Pk.class)
@Table(name = "document_type_company_code")
public class DocumentTypeCompanyCode {

    @Id
    @Column(name = "doc_type_code", length = 4)
    private String docTypeCode;

    @Id
    @Column(name = "company_code", length = 4)
    private String companyCode;

    @Column(name = "default_purch_group", length = 3)
    private String defaultPurchGroup;

    @Column(name = "doc_volume_2y", nullable = false)
    private Integer docVolume2y = 0;

    public DocumentTypeCompanyCode(String docTypeCode, String companyCode) {
        this.docTypeCode = docTypeCode;
        this.companyCode = companyCode;
    }

    @Data
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private String docTypeCode;
        private String companyCode;
    }
}
