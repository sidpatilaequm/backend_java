package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

public class PurchaseRoleDtos {

    @Data
    public static class AccessLevelOut {
        private String code;
        private String assigneeType;
        private String label;
        private String activities;
        private Integer sortOrder;
    }

    @Data
    public static class DocTypeAssignmentOut {
        private String companyCode;
        private String defaultPurchGroup;
        private Integer docVolume2y;
    }

    @Data
    public static class DocumentTypeOut {
        private String code;
        private String description;
        private String docCategory;
        private String classification;
        private String source;
        private List<DocTypeAssignmentOut> assignments;
    }

    @Data
    public static class DocTypeAssignmentIn {
        private String companyCode;
        private String defaultPurchGroup;
    }

    @Data
    public static class DocumentTypeIn {
        private String code;
        private String description;
        private String docCategory = "F";
        private String classification = "Product";
        private List<DocTypeAssignmentIn> assignments;
    }

    @Data
    public static class GrantIn {
        private String docTypeCode;
        private String companyCode;
        private String accessLevel;
    }

    @Data
    public static class GrantOut {
        private String docTypeCode;
        private String companyCode;
        private String accessLevel;
        private String docTypeDescription;
        private String docCategory;
        private String accessLabel;
        private String activities;
    }

    @Data
    public static class RoleIn {
        private String roleCode;
        private String roleName;
        private String assigneeType;
        private String assigneeRef;
        private LocalDate validTo;
        private List<String> companyCodes;
        private List<GrantIn> grants;
    }

    @Data
    public static class RoleSummaryOut {
        private Long id;
        private String roleCode;
        private String roleName;
        private String assigneeType;
        private String assigneeRef;
        private LocalDate validTo;
        private List<String> companyCodes;
        private int grantCount;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class RoleOut extends RoleSummaryOut {
        private List<GrantOut> grants;
    }
}
