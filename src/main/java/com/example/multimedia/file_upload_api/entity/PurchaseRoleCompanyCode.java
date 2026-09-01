package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** One company code in a {@link PurchaseRole}'s scope — a grant can't name a company code that
 *  isn't here (enforced at the database level, see {@link PurchaseRoleGrant} and the migration). */
@Data
@NoArgsConstructor
@Entity
@IdClass(PurchaseRoleCompanyCode.Pk.class)
@Table(name = "purchase_role_company_code")
public class PurchaseRoleCompanyCode {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "company_code", length = 4)
    private String companyCode;

    public PurchaseRoleCompanyCode(Long roleId, String companyCode) {
        this.roleId = roleId;
        this.companyCode = companyCode;
    }

    @Data
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private Long roleId;
        private String companyCode;
    }
}
