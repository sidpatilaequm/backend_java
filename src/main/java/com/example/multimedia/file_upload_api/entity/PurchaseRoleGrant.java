package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * One grant: this role may act on {@code docTypeCode} in {@code companyCode} at
 * {@code accessLevel}. Two composite foreign keys (declared in the migration SQL, not modeled as
 * JPA relations here — see {@link DocumentTypeCompanyCode}'s javadoc for why) are the actual
 * point of this design, enforced by the database itself, not just {@code PurchaseRoleService}:
 * (roleId, companyCode) must exist in purchase_role_company_code, and (docTypeCode, companyCode)
 * must exist in document_type_company_code.
 */
@Data
@NoArgsConstructor
@Entity
@IdClass(PurchaseRoleGrant.Pk.class)
@Table(name = "purchase_role_grant")
public class PurchaseRoleGrant {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "doc_type_code", length = 4)
    private String docTypeCode;

    @Id
    @Column(name = "company_code", length = 4)
    private String companyCode;

    @Column(name = "access_level", nullable = false, length = 10)
    private String accessLevel;

    public PurchaseRoleGrant(Long roleId, String docTypeCode, String companyCode, String accessLevel) {
        this.roleId = roleId;
        this.docTypeCode = docTypeCode;
        this.companyCode = companyCode;
        this.accessLevel = accessLevel;
    }

    @Data
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private Long roleId;
        private String docTypeCode;
        private String companyCode;
    }
}
