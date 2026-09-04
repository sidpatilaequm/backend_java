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

        List<Map<String, Object>> events = buildEvents(pr);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("prNumber", pr.getPrNumber());
        result.put("prStatus", pr.getStatus().name());
        result.put("requestedBy", resolveUserName(pr.getRequestedBy()));
        result.put("createdAt", toInstant(pr.getCreatedAt()));
        result.put("events", events);
        return result;
    }

    /**
     * Flat, cross-PR feed for the tab's default (no-search) view — same event shape as
     * getLifecycle, with prNumber attached to each event, across the most recently active PRs.
     * Pulls the N most recently created PRs (small real-world scale today — a handful of dozens,
     * not thousands — so per-PR aggregation here is cheap; revisit with a real cross-table query
     * if that stops being true) rather than paginating the underlying tables independently, so the
     * result stays one true chronological feed instead of 8 separately-paginated streams.
     */
    public Map<String, Object> getFeed(int page, int size) {
        int cappedSize = Math.min(Math.max(size, 1), 200);
        List<PurchaseRequisition> recentPrs = prRepo.findTop100ByOrderByCreatedAtDesc();

        List<Map<String, Object>> allEvents = new ArrayList<>();
        for (PurchaseRequisition pr : recentPrs) {
            for (Map<String, Object> e : buildEvents(pr)) {
                e.put("prNumber", pr.getPrNumber());
                allEvents.add(e);
            }
        }
        allEvents.sort(Comparator.comparing(
                (Map<String, Object> e) -> (Instant) e.get("timestamp"),
                Comparator.nullsLast(Comparator.reverseOrder())));

        int from = Math.min(page * cappedSize, allEvents.size());
        int to = Math.min(from + cappedSize, allEvents.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("events", allEvents.subList(from, to));
        result.put("page", page);
        result.put("totalPages", (int) Math.ceil(allEvents.size() / (double) cappedSize));
        result.put("totalElements", allEvents.size());
        return result;
    }

    private List<Map<String, Object>> buildEvents(PurchaseRequisition pr) {
        List<Map<String, Object>> events = new ArrayList<>();

        events.add(event("PR_CREATED", "PR Created", null,
                toInstant(pr.getCreatedAt()), resolveUserName(pr.getRequestedBy()), pr.getStatus().name(), pr.getPrNumber(),
                prDetails(pr)));

        if (pr.getWorkflowRequestId() != null) {
            workflowRequestRepo.findById(pr.getWorkflowRequestId().intValue()).ifPresent(wf -> {
                String submitterName = wf.getSubmitter() != null ? fullName(wf.getSubmitter()) : null;
                events.add(event("WORKFLOW_SUBMITTED", "Workflow Approval Submitted", null,
                        toInstant(wf.getSubmittedAt()), submitterName, wf.getStatus(), wf.getTitle(),
                        workflowDetails(wf)));

                for (ApprovalActionRO aa : approvalActionRepo.findByWorkflowRequestId(wf.getId())) {
                    String approverName = aa.getApprover() != null ? fullName(aa.getApprover()) : null;
                    String delegatedToName = aa.getDelegatedTo() != null ? fullName(aa.getDelegatedTo()) : null;
                    String detail = aa.getComment();
                    if (delegatedToName != null) {
                        detail = (detail == null ? "" : detail + " — ") + "delegated to " + delegatedToName;
                    }
                    events.add(event("WORKFLOW_ACTION", "Workflow Approval", approverName,
                            toInstant(aa.getActedAt()), approverName, aa.getDecision(), detail,
                            approvalActionDetails(aa)));
                }

                if (wf.getResolvedAt() != null) {
                    events.add(event("WORKFLOW_RESOLVED", "Workflow Approval Completed", null,
                            toInstant(wf.getResolvedAt()), null, wf.getStatus(), null,
                            workflowDetails(wf)));
                }
            });
        }

        for (PurchaseRequisitionItemVendor rv : rfqVendorRepo.findByPurchaseRequisitionItem_PurchaseRequisition_Id(pr.getId())) {
            String vendorName = resolveVendorMasterName(rv.getVendorId());
            events.add(event("RFQ_SENT", "RFQ Sent", vendorName,
                    toInstant(rv.getSentAt()), null, "SENT",
                    rv.getPurchaseRequisitionItem() != null ? "Item " + rv.getPurchaseRequisitionItem().getId() : null,
                    rfqVendorDetails(rv, vendorName)));
            if (rv.getRespondedAt() != null) {
                events.add(event("QUOTATION_ACK", "Quotation Acknowledged", vendorName,
                        toInstant(rv.getRespondedAt()), null, rv.getStatus(), null,
                        rfqVendorDetails(rv, vendorName)));
            }
        }

        for (VendorQuotation q : quotationRepo.findByPurchaseRequisition_Id(pr.getId())) {
            String vendorName = q.getVendor() != null ? q.getVendor().getCompanyName() : null;
            Map<String, Object> qDetails = quotationDetails(q);
            events.add(event("QUOTATION_SUBMITTED", "Quotation Sent to Company", vendorName,
                    toInstant(q.getCreatedDate()), null, q.getStatus(), q.getQuotationNumber(), qDetails));
            if ("AWARDED".equalsIgnoreCase(q.getStatus())) {
                events.add(event("QUOTATION_AWARDED", "Quotation Awarded", vendorName,
                        toInstant(q.getModifiedDate()), null, q.getStatus(), q.getQuotationNumber(), qDetails));
            }
        }

        for (PortalPurchaseOrder po : poRepo.findByPurchaseRequisition_Id(pr.getId())) {
            String vendorName = po.getVendor() != null ? po.getVendor().getCompanyName() : null;
            Map<String, Object> poDetails = poDetails(po);
            events.add(event("PO_GENERATED", "PO Generated", vendorName,
                    toInstant(po.getCreatedDate()), null, po.getStatus(), po.getPoNumber(), poDetails));
            if (po.getAcknowledgedAt() != null) {
                events.add(event("PO_ACK", "PO Acknowledged", vendorName,
                        toInstant(po.getAcknowledgedAt()), null, po.getStatus(), po.getPoNumber(), poDetails));
            }

            for (Asn asn : asnRepo.findByPurchaseOrder_Id(po.getId())) {
                events.add(event("ASN_SENT", "ASN Sent", vendorName,
                        toInstant(asn.getCreatedDate()), null, asn.getStatus(), asn.getInvoiceNumber(),
                        asnDetails(asn)));

                for (GateEntry ge : gateEntryRepo.findByAsnId(asn.getId())) {
                    events.add(event("GATE_ENTRY", "Gate Entry Created", vendorName,
                            toInstant(ge.getCreatedDate()), ge.getProcessedBy(), ge.getDecision(), ge.getGatePassNumber(),
                            gateEntryDetails(ge)));

                    goodsReceiptRepo.findByGateEntryId(ge.getId()).ifPresent(gr -> {
                        String detail = gr.getGrnNumber() != null ? gr.getGrnNumber() : gr.getRtvNumber();
                        events.add(event("MATERIAL_INWARD", "Material Inward", vendorName,
                                toInstant(gr.getCreatedDate()), gr.getProcessedBy(), gr.getDecision(), detail,
                                goodsReceiptDetails(gr)));
                    });
                }
            }
        }

        events.sort(Comparator.comparing(
                (Map<String, Object> e) -> (Instant) e.get("timestamp"),
                Comparator.nullsLast(Comparator.naturalOrder())));

        return events;
    }

    // ── Per-stage "everything there is to know about this" detail maps, shown when a step/chip
    // is clicked in the PR Lifecycle UI. Every field here already lives on the entity in hand —
    // this is purely a read-time projection, no extra queries beyond item lists.

    private Map<String, Object> prDetails(PurchaseRequisition pr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("PR Number", pr.getPrNumber());
        m.put("Status", pr.getStatus().name());
        m.put("Requested By", resolveUserName(pr.getRequestedBy()));
        m.put("Company Code", pr.getCompanyCode());
        m.put("Plant Code", pr.getPlantCode());
        m.put("Storage Location", pr.getSlocId());
        m.put("Required Date", pr.getRequiredDate());
        m.put("Total Amount", pr.getTotalAmount());
        m.put("Remarks", pr.getRemarks());
        m.put("Created At", toInstant(pr.getCreatedAt()));
        m.put("Updated At", toInstant(pr.getUpdatedAt()));
        m.put("Line Items", pr.getItems() == null ? List.of() : pr.getItems().stream().map(i -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("Material ID", i.getMaterialId());
            im.put("SKU", i.getSku());
            im.put("Quantity", i.getQuantity());
            im.put("UOM", i.getUom());
            im.put("Estimated Price", i.getEstimatedPrice());
            im.put("Total Price", i.getTotalPrice());
            im.put("Status", i.getStatus());
            return im;
        }).toList());
        return m;
    }

    private Map<String, Object> workflowDetails(com.example.multimedia.file_upload_api.entity.workflow.WorkflowRequestRO wf) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Title", wf.getTitle());
        m.put("Workflow", wf.getWorkflow() != null ? wf.getWorkflow().getName() : null);
        m.put("Department", wf.getDepartment());
        m.put("Request Type", wf.getRequestType());
        m.put("Amount", wf.getAmount());
        m.put("Status", wf.getStatus());
        m.put("Submitted By", wf.getSubmitter() != null ? fullName(wf.getSubmitter()) : null);
        m.put("Submitted At", toInstant(wf.getSubmittedAt()));
        m.put("Resolved At", toInstant(wf.getResolvedAt()));
        return m;
    }

    private Map<String, Object> approvalActionDetails(ApprovalActionRO aa) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Approver", aa.getApprover() != null ? fullName(aa.getApprover()) : null);
        m.put("Approver Email", aa.getApprover() != null ? aa.getApprover().getEmail() : null);
        m.put("Decision", aa.getDecision());
        m.put("Comment", aa.getComment());
        m.put("Delegated To", aa.getDelegatedTo() != null ? fullName(aa.getDelegatedTo()) : null);
        m.put("Acted At", toInstant(aa.getActedAt()));
        m.put("Stage Order", aa.getRequestStage() != null ? aa.getRequestStage().getStageOrder() : null);
        return m;
    }

    private Map<String, Object> rfqVendorDetails(PurchaseRequisitionItemVendor rv, String vendorName) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Vendor", vendorName);
        m.put("BP No", rv.getBpNo());
        m.put("Status", rv.getStatus());
        m.put("Item ID", rv.getPurchaseRequisitionItem() != null ? rv.getPurchaseRequisitionItem().getId() : null);
        m.put("Sent At", toInstant(rv.getSentAt()));
        m.put("Responded At", toInstant(rv.getRespondedAt()));
        return m;
    }

    private Map<String, Object> quotationDetails(VendorQuotation q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Quotation Number", q.getQuotationNumber());
        m.put("Vendor", q.getVendor() != null ? q.getVendor().getCompanyName() : null);
        m.put("Vendor Reference No", q.getVendorReferenceNo());
        m.put("Status", q.getStatus());
        m.put("Quotation Date", q.getQuotationDate());
        m.put("Valid Until", q.getValidUntil());
        m.put("Validity Days", q.getValidityDays());
        m.put("Currency", q.getCurrency());
        m.put("Incoterm", q.getIncoterm());
        m.put("Named Place", q.getNamedPlace());
        m.put("Quoted Delivery Date", q.getQuotedDeliveryDate());
        m.put("Lead Time (days)", q.getLeadTimeDays());
        m.put("Shipping Mode", q.getShippingMode());
        m.put("Freight Charge Type", q.getFreightChargeType());
        m.put("Freight Amount", q.getFreightAmount());
        m.put("Advance Required %", q.getAdvanceRequiredPercent());
        m.put("Bank Guarantee Required", q.getBankGuaranteeRequired());
        m.put("Subtotal", q.getSubtotalAmount());
        m.put("GST Total", q.getGstTotalAmount());
        m.put("Grand Total", q.getGrandTotalAmount());
        m.put("Cover Note", q.getCoverNote());
        m.put("Internal Notes", q.getInternalNotes());
        m.put("Created At", toInstant(q.getCreatedDate()));
        m.put("Modified At", toInstant(q.getModifiedDate()));
        m.put("Line Items", q.getItems() == null ? List.of() : q.getItems().stream().map(i -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("Item Code", i.getItemCode());
            im.put("Description", i.getDescription());
            im.put("PR Qty", i.getPrQty());
            im.put("Quoted Qty", i.getQuotedQty());
            im.put("UOM", i.getUom());
            im.put("Unit Price", i.getUnitPrice());
            im.put("GST %", i.getGstPercent());
            im.put("GST Amount", i.getGstAmount());
            im.put("Line Total", i.getLineTotal());
            im.put("Delivery Date", i.getDeliveryDate());
            return im;
        }).toList());
        return m;
    }

    private Map<String, Object> poDetails(PortalPurchaseOrder po) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("PO Number", po.getPoNumber());
        m.put("Vendor", po.getVendor() != null ? po.getVendor().getCompanyName() : null);
        m.put("Status", po.getStatus());
        m.put("PO Date", po.getPoDate());
        m.put("Company Code", po.getCompanyCode());
        m.put("Purchasing Doc Type", po.getPurchasingDocType());
        m.put("Purchasing Organization", po.getPurchasingOrganization());
        m.put("Purchasing Group", po.getPurchasingGroup());
        m.put("Currency", po.getCurrency());
        m.put("Incoterms", po.getIncoterms());
        m.put("Incoterms Part 2", po.getIncotermsPart2());
        m.put("Delivery Address", po.getDeliveryAddress());
        m.put("Requested Delivery Date", po.getRequestedDeliveryDate());
        m.put("Confirmed Delivery Date", po.getConfirmedDeliveryDate());
        m.put("Shipping Instructions", po.getShippingInstructions());
        m.put("Subtotal", po.getSubtotal());
        m.put("Freight Total", po.getFreightTotal());
        m.put("GST Total", po.getGstTotal());
        m.put("Grand Total", po.getGrandTotal());
        m.put("Remarks", po.getRemarks());
        m.put("Created By", po.getCreatedBy());
        m.put("Created At", toInstant(po.getCreatedDate()));
        m.put("Modified At", toInstant(po.getModifiedDate()));
        m.put("Acknowledged At", toInstant(po.getAcknowledgedAt()));
        m.put("Line Items", po.getItems() == null ? List.of() : po.getItems().stream().map(i -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("Line Number", i.getLineNumber());
            im.put("Material Number", i.getMaterialNumber());
            im.put("Material Description", i.getMaterialDescription());
            im.put("HSN Code", i.getHsnCode());
            im.put("Quantity", i.getQuantity());
            im.put("Shipped Quantity", i.getShippedQuantity());
            im.put("UOM", i.getUom());
            im.put("Unit Price", i.getUnitPrice());
            im.put("Net Value", i.getNetValue());
            im.put("Tax %", i.getTaxPercent());
            im.put("Tax Amount", i.getTaxAmount());
            im.put("Total Value", i.getTotalValue());
            return im;
        }).toList());
        return m;
    }

    private Map<String, Object> asnDetails(Asn asn) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Status", asn.getStatus());
        m.put("Gate Status", asn.getGateStatus());
        m.put("Gate Pass Number", asn.getGatePassNumber());
        m.put("Vendor BP No", asn.getVendorBpno());
        m.put("Company Code", asn.getCompanyCode());
        m.put("Invoice Number", asn.getInvoiceNumber());
        m.put("Invoice Date", asn.getInvoiceDate());
        m.put("Eway Bill", asn.getEwayBill());
        m.put("Eway Bill Valid To", asn.getEwbValidTo());
        m.put("Vehicle Number", asn.getVehicleNumber());
        m.put("Transporter Code", asn.getTransporterCode());
        m.put("Dispatch Date", asn.getDispatchDate());
        m.put("Expected Delivery", asn.getExpectedDelivery());
        m.put("Packaging", asn.getPackaging());
        m.put("No. of Packages", asn.getNoOfPackages());
        m.put("Created At", toInstant(asn.getCreatedDate()));
        m.put("Modified At", toInstant(asn.getModifiedDate()));
        m.put("Line Items", asn.getItems() == null ? List.of() : asn.getItems().stream().map(i -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("Part Number", i.getPartNumber());
            im.put("Quantity Shipped", i.getQuantityShipped());
            im.put("Batch/Heat Number", i.getBatchHeatNumber());
            return im;
        }).toList());
        return m;
    }

    private Map<String, Object> gateEntryDetails(GateEntry ge) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Gate Pass Number", ge.getGatePassNumber());
        m.put("Company Code", ge.getCompanyCode());
        m.put("Decision", ge.getDecision());
        m.put("Declared Packages", ge.getDeclaredPackages());
        m.put("Counted Packages", ge.getCountedPackages());
        m.put("Package Remark", ge.getPackageRemark());
        m.put("Hold Reason", ge.getHoldReason());
        m.put("In Time", ge.getInTime());
        m.put("Processed By", ge.getProcessedBy());
        m.put("Supervisor Remark", ge.getSupervisorRemark());
        m.put("Created At", toInstant(ge.getCreatedDate()));
        m.put("Modified At", toInstant(ge.getModifiedDate()));
        return m;
    }

    private Map<String, Object> goodsReceiptDetails(GoodsReceipt gr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Decision", gr.getDecision());
        m.put("GRN Number", gr.getGrnNumber());
        m.put("RTV Number", gr.getRtvNumber());
        m.put("Processed By", gr.getProcessedBy());
        m.put("Remarks", gr.getRemarks());
        m.put("Created At", toInstant(gr.getCreatedDate()));
        m.put("Modified At", toInstant(gr.getModifiedDate()));
        return m;
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
        return event(stage, stageLabel, branchKey, timestamp, actorName, status, detail, null);
    }

    private Map<String, Object> event(String stage, String stageLabel, String branchKey,
                                       Instant timestamp, String actorName, String status, String detail,
                                       Map<String, Object> details) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("stage", stage);
        m.put("stageLabel", stageLabel);
        m.put("branchKey", branchKey);
        m.put("timestamp", timestamp);
        m.put("actorName", actorName);
        m.put("status", status);
        m.put("detail", detail);
        m.put("details", details);
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
