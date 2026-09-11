package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * One vendor's business-type classification (Product/Service/Subcontracting/Scheduling
 * Agreement), scoped to one company code -- the per-company alternative to the uniform
 * vendor_type_* flags on SupplierRegistration/CompanyDetails. A vendor can be Product-only for
 * company 1000 and Service-only for company 2000, same "scoped to a company code" shape as
 * SupplierRegistrationDocumentType.
 */
@Data
@NoArgsConstructor
@Entity
@IdClass(SupplierRegistrationCompanyBusinessType.Pk.class)
@Table(name = "supplier_registration_company_business_type")
public class SupplierRegistrationCompanyBusinessType {

    @Id
    @Column(name = "registration_id")
    private Long registrationId;

    @Id
    @Column(name = "company_code", length = 4)
    private String companyCode;

    @Column(name = "vendor_type_product")
    private boolean vendorTypeProduct = false;

    @Column(name = "vendor_type_service")
    private boolean vendorTypeService = false;

    @Column(name = "vendor_type_subcontracting")
    private boolean vendorTypeSubcontracting = false;

    @Column(name = "vendor_type_scheduling_agreement")
    private boolean vendorTypeSchedulingAgreement = false;

    public SupplierRegistrationCompanyBusinessType(Long registrationId, String companyCode) {
        this.registrationId = registrationId;
        this.companyCode = companyCode;
    }

    @Data
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private Long registrationId;
        private String companyCode;
    }
}
