package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A document type an approver picked for a vendor application, scoped to one company code — the
 * replacement for the old flat vendorCategory string. A vendor can be granted different document
 * types per company code (e.g. Products in 1000 but not 2000), so this is a three-part key rather
 * than one column on SupplierRegistration. The (docTypeCode, companyCode) pair is a real foreign
 * key into document_type_company_code (see the migration SQL) — an approver can't pick a document
 * type that was never assigned to that company code, enforced at the database level, same pattern
 * as PurchaseRoleGrant.
 */
@Data
@NoArgsConstructor
@Entity
@IdClass(SupplierRegistrationDocumentType.Pk.class)
@Table(name = "supplier_registration_document_type")
public class SupplierRegistrationDocumentType {

    @Id
    @Column(name = "registration_id")
    private Long registrationId;

    @Id
    @Column(name = "company_code", length = 4)
    private String companyCode;

    @Id
    @Column(name = "doc_type_code", length = 4)
    private String docTypeCode;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public SupplierRegistrationDocumentType(Long registrationId, String companyCode, String docTypeCode) {
        this.registrationId = registrationId;
        this.companyCode = companyCode;
        this.docTypeCode = docTypeCode;
    }

    @Data
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private Long registrationId;
        private String companyCode;
        private String docTypeCode;
    }
}
