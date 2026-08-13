package com.example.multimedia.file_upload_api.config;

import com.example.multimedia.file_upload_api.entity.PermissionMaster;
import com.example.multimedia.file_upload_api.enums.PermissionType;
import com.example.multimedia.file_upload_api.repository.PermissionMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PermissionDataInitializer implements CommandLineRunner {

    @Autowired
    private PermissionMasterRepository permissionMasterRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.AuthorizationRepository authorizationRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize Authorizations
        ensureAuthorization("super_admin", "Super Admin");
        ensureAuthorization("vendor", "Vendor");
        ensureAuthorization("customer", "Customer");
        ensureAuthorization("administrator", "Administrator");
        ensureAuthorization("procurement_manager", "Procurement Manager");
        ensureAuthorization("employee", "Employee");

        // --- 1. Purchase Requisition ---
        PermissionMaster pr = ensurePermission("PURCHASE_REQUISITION", "Purchase Requisition", PermissionType.MODULE,
                null);
        ensurePermission("PR_HEADER", "Header Information", PermissionType.BLOCK, pr);
        ensurePermission("PR_LINE_ITEM", "Line Item Details", PermissionType.BLOCK, pr);

        // --- 2. Quotation ---
        PermissionMaster quote = ensurePermission("QUOTATION", "Quotation", PermissionType.MODULE, null);
        ensurePermission("QUOTE_HEADER", "Header Information", PermissionType.BLOCK, quote);
        ensurePermission("QUOTE_VENDOR_INFO", "Vendor Information", PermissionType.BLOCK, quote);
        ensurePermission("QUOTE_LINE_ITEM", "Line Item Details", PermissionType.BLOCK, quote);

        // --- 3. Purchase Order ---
        PermissionMaster po = ensurePermission("PURCHASE_ORDER", "Purchase Order", PermissionType.MODULE, null);

        PermissionMaster spo = ensurePermission("SPO", "Subcontracting Purchase Order", PermissionType.SUB_MODULE, po);
        ensurePermission("SPO_HEADER", "Header Information", PermissionType.BLOCK, spo);
        ensurePermission("SPO_COMPONENT", "Component Details", PermissionType.BLOCK, spo);

        ensurePermission("SCHEDULING_AGREEMENT", "Scheduling Agreement", PermissionType.SUB_MODULE, po);
        ensurePermission("SERVICE_PO", "Service Purchase Order", PermissionType.SUB_MODULE, po);

        // --- 4. ASN ---
        PermissionMaster asn = ensurePermission("ASN", "ASN", PermissionType.MODULE, null);
        ensurePermission("ASN_HEADER", "Header Information", PermissionType.BLOCK, asn);
        ensurePermission("ASN_SHIPMENT", "Shipment Details", PermissionType.BLOCK, asn);
        ensurePermission("ASN_LINE_ITEM", "Line Item Details", PermissionType.BLOCK, asn);

        // --- 5. Inventory ---
        PermissionMaster inventory = ensurePermission("INVENTORY", "Inventory", PermissionType.MODULE, null);
        ensurePermission("INV_HEADER", "Header Information", PermissionType.BLOCK, inventory);
        ensurePermission("INV_STOCK", "Stock Details", PermissionType.BLOCK, inventory);

        // --- 6. Invoice ---
        PermissionMaster invoice = ensurePermission("INVOICE", "Invoice", PermissionType.MODULE, null);
        ensurePermission("INV_HEADER_INFO", "Header Information", PermissionType.BLOCK, invoice);
        ensurePermission("INV_ITEM_DETAILS", "Item Details", PermissionType.BLOCK, invoice);
        ensurePermission("INV_TAX_DETAILS", "Tax Details", PermissionType.BLOCK, invoice);

        // --- 7. Vendor Payments ---
        PermissionMaster payments = ensurePermission("VENDOR_PAYMENTS", "Vendor Payments", PermissionType.MODULE, null);
        ensurePermission("PAY_OVERVIEW", "Payment Overview", PermissionType.BLOCK, payments);
        ensurePermission("PAY_HISTORY", "Transaction History", PermissionType.BLOCK, payments);

        // --- 8. Vendor Returns ---
        PermissionMaster returns = ensurePermission("VENDOR_RETURNS", "Vendor Returns", PermissionType.MODULE, null);
        ensurePermission("RET_HEADER", "Header Information", PermissionType.BLOCK, returns);
        ensurePermission("RET_ITEM", "Item Details", PermissionType.BLOCK, returns);

        // --- 9. Credit ---
        PermissionMaster credit = ensurePermission("CREDIT", "Credit", PermissionType.MODULE, null);
        ensurePermission("CRED_HEADER", "Header Information", PermissionType.BLOCK, credit);
        ensurePermission("CRED_DETAILS", "Credit Details", PermissionType.BLOCK, credit);

        // --- 10. Credit Payment ---
        PermissionMaster credPay = ensurePermission("CREDIT_PAYMENT", "Credit Payment", PermissionType.MODULE, null);
        ensurePermission("CPAY_HEADER", "Header Information", PermissionType.BLOCK, credPay);
        ensurePermission("CPAY_DETAILS", "Payment Details", PermissionType.BLOCK, credPay);

        // General Vendor Info (Global)
        ensurePermission("VENDOR_INFO", "Global Vendor Information", PermissionType.MODULE, null);
    }

    private PermissionMaster ensurePermission(String code, String name, PermissionType type, PermissionMaster parent) {
        Optional<PermissionMaster> existing = permissionMasterRepository.findByCode(code);
        if (existing.isPresent()) {
            return existing.get();
        }

        PermissionMaster pm = new PermissionMaster();
        pm.setCode(code);
        pm.setName(name);
        pm.setType(type);
        pm.setParent(parent);
        return permissionMasterRepository.save(pm);
    }

    private void ensureAuthorization(String key, String name) {
        Optional<com.example.multimedia.file_upload_api.entity.Authorization> existing = authorizationRepository.findByAuthKeyIgnoreCase(key);
        if (existing.isEmpty()) {
            com.example.multimedia.file_upload_api.entity.Authorization auth = new com.example.multimedia.file_upload_api.entity.Authorization();
            auth.setAuthKey(key);
            auth.setAuthName(name);
            auth.setActive(true);
            authorizationRepository.save(auth);
        }
    }
}
