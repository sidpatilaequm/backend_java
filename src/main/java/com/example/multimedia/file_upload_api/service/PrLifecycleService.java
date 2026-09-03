package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.entity.workflow.ApprovalActionRO;
import com.example.multimedia.file_upload_api.repository.*;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Read-only aggregator for the Audit Log's "PR Lifecycle" tab — walks every table a PR's approval
 * and fulfillment can touch (workflow_requests/request_stages/approval_actions,
 * purchase_requisition_item_vendors, vendor_quotations, portal_purchase_orders, asns,
 * gate_entries, goods_receipts) and assembles one chronological event list. Never writes anything;
 * every FK in this chain is a real column already on the relevant entity except PR->WorkflowRequest,
 * which is a plain value match (purchase_requisitions.workflow_request_id = workflow_requests.id,
 * no DB-level FK — the two tables are owned by different apps).
 */
@Service
public class PrLifecycleService {

    private final PurchaseRequisitionRepository prRepo;
    private final WorkflowRequestRepository workflowRequestRepo;
    private final ApprovalActionRepository approvalActionRepo;
    private final PurchaseRequisitionItemVendorRepository rfqVendorRepo;
    private final VendorQuotationRepository quotationRepo;
    private final PortalPurchaseOrderRepository poRepo;
    private final AsnRepository asnRepo;
    private final GateEntryRepository gateEntryRepo;
    private final GoodsReceiptRepository goodsReceiptRepo;
    private final UserDetailRepository userDetailRepo;
    private final VendorMasterRepository vendorMasterRepo;

    public PrLifecycleService(PurchaseRequisitionRepository prRepo,
                               WorkflowRequestRepository workflowRequestRepo,
                               ApprovalActionRepository approvalActionRepo,
                               PurchaseRequisitionItemVendorRepository rfqVendorRepo,
                               VendorQuotationRepository quotationRepo,
                               PortalPurchaseOrderRepository poRepo,
                               AsnRepository asnRepo,
                               GateEntryRepository gateEntryRepo,
                               GoodsReceiptRepository goodsReceiptRepo,
                               UserDetailRepository userDetailRepo,
                               VendorMasterRepository vendorMasterRepo) {
        this.prRepo = prRepo;
        this.workflowRequestRepo = workflowRequestRepo;
        this.approvalActionRepo = approvalActionRepo;
        this.rfqVendorRepo = rfqVendorRepo;
        this.quotationRepo = quotationRepo;
        this.poRepo = poRepo;
        this.asnRepo = asnRepo;
        this.gateEntryRepo = gateEntryRepo;
        this.goodsReceiptRepo = goodsReceiptRepo;
        this.userDetailRepo = userDetailRepo;
        this.vendorMasterRepo = vendorMasterRepo;
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    public Map<String, Object> getLifecycle(String prNumber) {
        PurchaseRequisition pr = prRepo.findByPrNumber(prNumber)
                .orElseThrow(() -> new NotFoundException("No PR found with number " + prNumber));

        List<Map<String, Object>> events = new ArrayList<>();

        events.add(event("PR_CREATED", "PR Created", null,
                toInstant(pr.getCreatedAt()), resolveUserName(pr.getRequestedBy()), pr.getStatus().name(), pr.getPrNumber()));

        if (pr.getWorkflowRequestId() != null) {
            workflowRequestRepo.findById(pr.getWorkflowRequestId().intValue()).ifPresent(wf -> {
                String submitterName = wf.getSubmitter() != null ? fullName(wf.getSubmitter()) : null;
                events.add(event("WORKFLOW_SUBMITTED", "Workflow Approval Submitted", null,
                        toInstant(wf.getSubmittedAt()), submitterName, wf.getStatus(), wf.getTitle()));

                for (ApprovalActionRO aa : approvalActionRepo.findByWorkflowRequestId(wf.getId())) {
                    String approverName = aa.getApprover() != null ? fullName(aa.getApprover()) : null;
                    String delegatedToName = aa.getDelegatedTo() != null ? fullName(aa.getDelegatedTo()) : null;
                    String detail = aa.getComment();
                    if (delegatedToName != null) {
                        detail = (detail == null ? "" : detail + " — ") + "delegated to " + delegatedToName;
                    }
                    events.add(event("WORKFLOW_ACTION", "Workflow Approval", approverName,
                            toInstant(aa.getActedAt()), approverName, aa.getDecision(), detail));
                }

                if (wf.getResolvedAt() != null) {
                    events.add(event("WORKFLOW_RESOLVED", "Workflow Approval Completed", null,
                            toInstant(wf.getResolvedAt()), null, wf.getStatus(), null));
                }
            });
        }

        for (PurchaseRequisitionItemVendor rv : rfqVendorRepo.findByPurchaseRequisitionItem_PurchaseRequisition_Id(pr.getId())) {
            String vendorName = resolveVendorMasterName(rv.getVendorId());
            events.add(event("RFQ_SENT", "RFQ Sent", vendorName,
                    toInstant(rv.getSentAt()), null, "SENT",
                    rv.getPurchaseRequisitionItem() != null ? "Item " + rv.getPurchaseRequisitionItem().getId() : null));
            if (rv.getRespondedAt() != null) {
                events.add(event("QUOTATION_ACK", "Quotation Acknowledged", vendorName,
                        toInstant(rv.getRespondedAt()), null, rv.getStatus(), null));
            }
        }

        for (VendorQuotation q : quotationRepo.findByPurchaseRequisition_Id(pr.getId())) {
            String vendorName = q.getVendor() != null ? q.getVendor().getCompanyName() : null;
            events.add(event("QUOTATION_SUBMITTED", "Quotation Sent to Company", vendorName,
                    toInstant(q.getCreatedDate()), null, q.getStatus(), q.getQuotationNumber()));
            if ("AWARDED".equalsIgnoreCase(q.getStatus())) {
                events.add(event("QUOTATION_AWARDED", "Quotation Awarded", vendorName,
                        toInstant(q.getModifiedDate()), null, q.getStatus(), q.getQuotationNumber()));
            }
        }

        for (PortalPurchaseOrder po : poRepo.findByPurchaseRequisition_Id(pr.getId())) {
            String vendorName = po.getVendor() != null ? po.getVendor().getCompanyName() : null;
            events.add(event("PO_GENERATED", "PO Generated", vendorName,
                    toInstant(po.getCreatedDate()), null, po.getStatus(), po.getPoNumber()));
            if (po.getAcknowledgedAt() != null) {
                events.add(event("PO_ACK", "PO Acknowledged", vendorName,
                        toInstant(po.getAcknowledgedAt()), null, po.getStatus(), po.getPoNumber()));
            }

            for (Asn asn : asnRepo.findByPurchaseOrder_Id(po.getId())) {
                events.add(event("ASN_SENT", "ASN Sent", vendorName,
                        toInstant(asn.getCreatedDate()), null, asn.getStatus(), asn.getInvoiceNumber()));

                for (GateEntry ge : gateEntryRepo.findByAsnId(asn.getId())) {
                    events.add(event("GATE_ENTRY", "Gate Entry Created", vendorName,
                            toInstant(ge.getCreatedDate()), ge.getProcessedBy(), ge.getDecision(), ge.getGatePassNumber()));

                    goodsReceiptRepo.findByGateEntryId(ge.getId()).ifPresent(gr -> {
                        String detail = gr.getGrnNumber() != null ? gr.getGrnNumber() : gr.getRtvNumber();
                        events.add(event("MATERIAL_INWARD", "Material Inward", vendorName,
                                toInstant(gr.getCreatedDate()), gr.getProcessedBy(), gr.getDecision(), detail));
                    });
                }
            }
        }

        events.sort(Comparator.comparing(
                (Map<String, Object> e) -> (Instant) e.get("timestamp"),
                Comparator.nullsLast(Comparator.naturalOrder())));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("prNumber", pr.getPrNumber());
        result.put("prStatus", pr.getStatus().name());
        result.put("requestedBy", resolveUserName(pr.getRequestedBy()));
        result.put("createdAt", toInstant(pr.getCreatedAt()));
        result.put("events", events);
        return result;
    }

    /** PR numbers matching a search string — backs the tab's typeahead. */
    public List<String> searchPrNumbers(String q, int limit) {
        if (q == null || q.trim().isEmpty()) return List.of();
        return prRepo.findTop20ByPrNumberContainingIgnoreCaseOrderByCreatedAtDesc(q.trim()).stream()
                .limit(Math.min(Math.max(limit, 1), 20))
                .map(PurchaseRequisition::getPrNumber)
                .toList();
    }

    private Map<String, Object> event(String stage, String stageLabel, String branchKey,
                                       Instant timestamp, String actorName, String status, String detail) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("stage", stage);
        m.put("stageLabel", stageLabel);
        m.put("branchKey", branchKey);
        m.put("timestamp", timestamp);
        m.put("actorName", actorName);
        m.put("status", status);
        m.put("detail", detail);
        return m;
    }

    private String resolveUserName(Long userId) {
        if (userId == null) return null;
        return userDetailRepo.findById(userId).map(this::fullName).orElse(null);
    }

    // purchase_requisition_item_vendors.vendor_id is keyed by VendorMaster.vendor_id (not
    // CompanyDetails.company_id — see AuthController's vendorMasterId fix for why those two IDs
    // are not interchangeable in this codebase), so resolving a display name here goes through
    // VendorMaster -> its linked SupplierRegistration's vendor name, not through CompanyDetails.
    private String resolveVendorMasterName(Long vendorMasterId) {
        if (vendorMasterId == null) return null;
        return vendorMasterRepo.findById(vendorMasterId)
                .map(vm -> vm.getSupplierRegistration() != null ? vm.getSupplierRegistration().getVendorName() : null)
                .orElse(null);
    }

    private String fullName(UserDetail u) {
        String f = u.getFirstName() == null ? "" : u.getFirstName().trim();
        String l = u.getLastName() == null ? "" : u.getLastName().trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? u.getEmail() : full;
    }

    private static Instant toInstant(Timestamp t) {
        return t == null ? null : t.toInstant();
    }

    private static Instant toInstant(LocalDateTime t) {
        return t == null ? null : t.atZone(ZoneId.systemDefault()).toInstant();
    }
}
