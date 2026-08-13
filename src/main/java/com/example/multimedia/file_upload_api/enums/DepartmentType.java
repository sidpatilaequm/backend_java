package com.example.multimedia.file_upload_api.enums;

/**
 * Master list of all departments in the system.
 *
 * Each constant carries:
 *  - code        : the primary key stored in the `department` table (dept_code)
 *  - displayName : the human-readable label shown in the UI dropdown
 *  - description : a short functional description of the department
 *
 * Usage:
 *   DepartmentType.PURCHASE_DEPT.getCode()        → "DEPT-PUR"
 *   DepartmentType.PURCHASE_DEPT.getDisplayName() → "Purchase Department"
 *   DepartmentType.PURCHASE_DEPT.getDescription() → "Handles procurement ..."
 */
public enum DepartmentType {

    PURCHASE_DEPT(
            "DEPT-PUR",
            "Purchase Department",
            "Handles procurement, purchase requisitions, RFQs, POs, and vendor management."
    ),
    SALES_DEPT(
            "DEPT-SAL",
            "Sales Department",
            "Manages customer orders, quotations, invoices, and sales operations."
    ),
    INVENTORY_DEPT(
            "DEPT-INV",
            "Inventory Department",
            "Controls stock, warehouses, stock transfers, and inventory adjustments."
    ),
    WAREHOUSE_DEPT(
            "DEPT-WHS",
            "Warehouse Department",
            "Manages receiving, picking, packing, shipping, and storage."
    ),
    PRODUCTION_DEPT(
            "DEPT-PRD",
            "Production Department",
            "Handles manufacturing, work orders, BOM, and production planning."
    ),
    QUALITY_CONTROL(
            "DEPT-QC",
            "Quality Control (QC)",
            "Performs inspections, testing, and quality approvals."
    ),
    QUALITY_ASSURANCE(
            "DEPT-QA",
            "Quality Assurance (QA)",
            "Defines quality standards and compliance processes."
    ),
    LOGISTICS_DEPT(
            "DEPT-LOG",
            "Logistics Department",
            "Manages transportation, deliveries, and shipment tracking."
    ),
    SUPPLY_CHAIN_DEPT(
            "DEPT-SC",
            "Supply Chain Department",
            "Oversees end-to-end supply chain planning and coordination."
    ),
    PROCUREMENT_DEPT(
            "DEPT-PROC",
            "Procurement Department",
            "Strategic sourcing, supplier negotiations, and contract management."
    ),
    FINANCE_DEPT(
            "DEPT-FIN",
            "Finance Department",
            "Accounts payable, receivable, payments, budgeting, and reporting."
    ),
    ACCOUNTS_DEPT(
            "DEPT-ACC",
            "Accounts Department",
            "Bookkeeping, journals, ledgers, and reconciliations."
    ),
    HR_DEPT(
            "DEPT-HR",
            "Human Resources (HR)",
            "Employee management, payroll, attendance, and recruitment."
    ),
    ADMINISTRATION(
            "DEPT-ADM",
            "Administration",
            "Office administration and facility management."
    ),
    IT_DEPT(
            "DEPT-IT",
            "Information Technology (IT)",
            "ERP support, infrastructure, security, and user management."
    ),
    CUSTOMER_SUPPORT(
            "DEPT-CS",
            "Customer Support",
            "Handles customer complaints, returns, and service requests."
    ),
    VENDOR_MANAGEMENT(
            "DEPT-VM",
            "Vendor Management",
            "Vendor onboarding, evaluation, and performance monitoring."
    ),
    ENGINEERING_DEPT(
            "DEPT-ENG",
            "Engineering Department",
            "Product design, engineering changes, and technical documentation."
    ),
    MAINTENANCE_DEPT(
            "DEPT-MNT",
            "Maintenance Department",
            "Equipment maintenance and preventive maintenance schedules."
    ),
    PLANNING_DEPT(
            "DEPT-PLN",
            "Planning Department",
            "Material planning, production planning, and demand forecasting."
    ),
    EXPORT_DEPT(
            "DEPT-EXP",
            "Export Department",
            "Export documentation, customs, and international shipments."
    ),
    IMPORT_DEPT(
            "DEPT-IMP",
            "Import Department",
            "Import documentation and customs clearance."
    ),
    COMPLIANCE_DEPT(
            "DEPT-COMP",
            "Compliance Department",
            "Regulatory compliance, audits, and certifications."
    ),
    LEGAL_DEPT(
            "DEPT-LEG",
            "Legal Department",
            "Contracts, legal compliance, and dispute management."
    ),
    PMO(
            "DEPT-PMO",
            "Project Management Office (PMO)",
            "Project planning, execution, and monitoring."
    ),
    RND_DEPT(
            "DEPT-RND",
            "Research & Development (R&D)",
            "Product innovation and research activities."
    ),
    MARKETING_DEPT(
            "DEPT-MKT",
            "Marketing Department",
            "Marketing campaigns and product promotions."
    ),
    ECOMMERCE_DEPT(
            "DEPT-ECM",
            "E-Commerce Department",
            "Online marketplace and website order management."
    ),
    RETAIL_OPS(
            "DEPT-RET",
            "Retail Operations",
            "Store management and POS operations."
    ),
    BI_DEPT(
            "DEPT-BI",
            "Business Intelligence (BI)",
            "Dashboards, reports, and analytics."
    );

    // ── Fields ────────────────────────────────────────────────────────────────

    private final String code;
    private final String displayName;
    private final String description;

    // ── Constructor ───────────────────────────────────────────────────────────

    DepartmentType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
