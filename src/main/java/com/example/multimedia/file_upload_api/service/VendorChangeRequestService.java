package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.SupplierChangeRequest;
import com.example.multimedia.file_upload_api.entity.SupplierRegistration;
import com.example.multimedia.file_upload_api.entity.SupplierRegistrationAttachment;
import com.example.multimedia.file_upload_api.entity.SupplierRegistrationDocument;
import com.example.multimedia.file_upload_api.repository.SupplierChangeRequestRepository;
import com.example.multimedia.file_upload_api.repository.SupplierRegistrationAttachmentRepository;
import com.example.multimedia.file_upload_api.repository.SupplierRegistrationDocumentRepository;
import com.example.multimedia.file_upload_api.repository.SupplierRegistrationRepository;
import com.example.multimedia.file_upload_api.util.SupplierDocumentConfig;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * A vendor's self-service request to change one already-approved document, attachment, or
 * questionnaire answer. Goes through its own "Vendor Change Request" WorkFlow workflow (same
 * approver_group as the original "Vendor Approval" workflow, per the user's ask — a separate
 * workflow definition rather than the same one, so change requests don't get mixed in with new
 * applicant reviews in the admin's request list). The proposed new value is only actually
 * applied to SupplierRegistration/its documents/Form Studio's answer tables once that request
 * is approved — see applyApprovedChange.
 */
@Service
public class VendorChangeRequestService {

    private static final Logger logger = LoggerFactory.getLogger(VendorChangeRequestService.class);

    private final SupplierChangeRequestRepository changeRequestRepository;
    private final SupplierRegistrationRepository registrationRepository;
    private final SupplierRegistrationDocumentRepository documentRepository;
    private final SupplierRegistrationAttachmentRepository attachmentRepository;
    private final FolderItService folderItService;
    private final QuestionnaireService questionnaireService;
    private final OpenAiVisionOcrService ocrService;
    private final RestTemplate restTemplate;
    private final ServiceControllerUtils serviceControllerUtils;

    @Value("${workflow.api.base-url:http://localhost:8000}")
    private String workflowBaseUrl;

    @Value("${workflow.vendor-change-request.id:13}")
    private Long changeRequestWorkflowId;

    private final WorkflowEmailClient workflowEmailClient;

    public VendorChangeRequestService(SupplierChangeRequestRepository changeRequestRepository,
                                       SupplierRegistrationRepository registrationRepository,
                                       SupplierRegistrationDocumentRepository documentRepository,
                                       SupplierRegistrationAttachmentRepository attachmentRepository,
                                       FolderItService folderItService,
                                       QuestionnaireService questionnaireService,
                                       OpenAiVisionOcrService ocrService,
                                       RestTemplate restTemplate,
                                       ServiceControllerUtils serviceControllerUtils,
                                       WorkflowEmailClient workflowEmailClient) {
        this.changeRequestRepository = changeRequestRepository;
        this.registrationRepository = registrationRepository;
        this.documentRepository = documentRepository;
        this.attachmentRepository = attachmentRepository;
        this.folderItService = folderItService;
        this.questionnaireService = questionnaireService;
        this.ocrService = ocrService;
        this.restTemplate = restTemplate;
        this.serviceControllerUtils = serviceControllerUtils;
        this.workflowEmailClient = workflowEmailClient;
    }

    /**
     * itemType: "document" (itemKey = docType, e.g. "gst") | "attachment" (itemKey = attachment
     * id) | "answer" (itemKey = questionId). file is required for document/attachment; newAnswerJson
     * (the same {textValue?/optionIds?/rows?} shape used everywhere else) is required for answer.
     * vendorEmail is the caller's own JWT login email — never a caller-supplied id — so a vendor
     * can only ever raise a change request against their own registration.
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse submit(String vendorEmail, String itemType, String itemKey, String reason,
                                   MultipartFile file, String newAnswerJson) {
        ServiceResponse response = new ServiceResponse();
        try {
            SupplierRegistration reg = registrationRepository.findByEmail(vendorEmail)
                    .orElseThrow(() -> new RuntimeException("No supplier profile found for this account"));
            if (reason == null || reason.isBlank()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "A reason is required.");
            }

            String oldValueSummary;
            String newFileName = null;
            String newFolderItFileUid = null;

            switch (itemType) {
                case "document" -> {
                    SupplierRegistrationDocument doc = documentRepository.findByRegistrationIdAndDocType(reg.getId(), itemKey).orElse(null);
                    oldValueSummary = doc != null ? doc.getFileName() : "(not uploaded yet)";
                    if (file == null || file.isEmpty()) {
                        return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Attach the replacement file.");
                    }
                    newFolderItFileUid = folderItService.uploadFileToFolder(file, reg.getFolderitFolderUid());
                    newFileName = file.getOriginalFilename();
                }
                case "attachment" -> {
                    SupplierRegistrationAttachment att = attachmentRepository.findById(Long.parseLong(itemKey)).orElse(null);
                    if (att == null || !reg.getId().equals(att.getRegistration().getId())) {
                        return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Attachment not found.");
                    }
                    oldValueSummary = att.getFileName();
                    if (file == null || file.isEmpty()) {
                        return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Attach the replacement file.");
                    }
                    newFolderItFileUid = folderItService.uploadFileToFolder(file, reg.getFolderitFolderUid());
                    newFileName = file.getOriginalFilename();
                }
                case "answer" -> {
                    Integer questionId = Integer.parseInt(itemKey);
                    oldValueSummary = questionnaireService.getAnswerSummary(reg.getFormStudioResponseId(), questionId);
                    if (newAnswerJson == null || newAnswerJson.isBlank()) {
                        return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Provide the new answer.");
                    }
                }
                default -> {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Unknown item type.");
                }
            }

            SupplierChangeRequest cr = new SupplierChangeRequest();
            cr.setRegistration(reg);
            cr.setItemType(itemType);
            cr.setItemKey(itemKey);
            cr.setReason(reason.trim());
            cr.setOldValueSummary(oldValueSummary);
            cr.setNewFileName(newFileName);
            cr.setNewFolderItFileUid(newFolderItFileUid);
            cr.setNewAnswerJson(newAnswerJson);
            cr = changeRequestRepository.save(cr);

            JSONObject metadata = new JSONObject()
                    .put("changeRequestId", cr.getId())
                    .put("registrationId", reg.getId())
                    .put("vendorName", reg.getVendorName())
                    .put("contactName", reg.getContactName())
                    .put("email", reg.getEmail())
                    .put("itemType", itemType)
                    .put("itemKey", itemKey)
                    .put("reason", cr.getReason());

            JSONObject payload = new JSONObject()
                    .put("title", "Change request — " + reg.getVendorName() + " — " + itemType + ": " + itemKey)
                    .put("description", cr.getReason())
                    .put("request_type", "vendor_change_request")
                    .put("department", JSONObject.NULL)
                    .put("workflow_id", changeRequestWorkflowId)
                    .put("request_metadata", metadata);

            String url = UriComponentsBuilder.fromHttpUrl(workflowBaseUrl + "/api/requests/")
                    .queryParam("user_id", reg.getUserId())
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            JSONObject wfResponse = new JSONObject(restTemplate.postForObject(url, request, String.class));

            cr.setWorkflowRequestId(wfResponse.getLong("id"));
            changeRequestRepository.save(cr);

            Map<String, Object> submittedVars = new HashMap<>();
            submittedVars.put("contact_name", orDefault(reg.getContactName(), "there"));
            submittedVars.put("vendor_name", orDefault(reg.getVendorName(), "your company"));
            submittedVars.put("item_label", itemLabel(cr));
            submittedVars.put("reason", cr.getReason());
            triggerWorkflowEmail("VCR.1", reg.getEmail(), submittedVars);

            Map<String, Object> data = new HashMap<>();
            data.put("changeRequestId", cr.getId());
            response.addData("result", data);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Change request submitted for review");
        } catch (NumberFormatException e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Invalid item reference.");
        } catch (IOException | RuntimeException e) {
            logger.error("Change request submission failed", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to submit change request: " + e.getMessage());
        }
    }

    /**
     * Called from SupplierRegistrationService.handleApprovalWebhook for every incoming webhook,
     * before it tries its own SupplierRegistration lookup — change requests share that same
     * webhook endpoint (both workflows point at it) but are keyed by their own
     * SupplierChangeRequest.workflowRequestId, never SupplierRegistration.workflowRequestId.
     * Returns false (does nothing) when workflowRequestId doesn't belong to a change request, so
     * the caller falls through to its own handling.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handleWebhookIfChangeRequest(long workflowRequestId, String event) {
        SupplierChangeRequest cr = changeRequestRepository.findByWorkflowRequestId(workflowRequestId).orElse(null);
        if (cr == null) return false;

        String status;
        if ("request.approved".equals(event)) status = "APPROVED";
        else if ("request.rejected".equals(event) || "request.cancelled".equals(event)) status = "REJECTED";
        else if ("request.escalated".equals(event)) status = "ESCALATED";
        else return true;

        // WorkFlow's outgoing webhook is known to fire more than once per decision — this claim
        // is the only thing standing between that and a change applied twice / VCR.2 sent twice.
        int claimed = changeRequestRepository.markDecided(cr.getId(), status, LocalDateTime.now());
        if (claimed == 0) {
            logger.info("Change request {} already decided — ignoring duplicate webhook", cr.getId());
            return true;
        }

        if ("APPROVED".equals(status)) {
            applyApprovedChange(cr);
        }
        if ("ESCALATED".equals(status)) {
            return true;
        }

        SupplierRegistration reg = cr.getRegistration();
        String decision = "APPROVED".equals(status) ? "approved" : "rejected";
        Map<String, Object> decidedVars = new HashMap<>();
        decidedVars.put("contact_name", orDefault(reg.getContactName(), "there"));
        decidedVars.put("vendor_name", orDefault(reg.getVendorName(), "your company"));
        decidedVars.put("item_label", itemLabel(cr));
        decidedVars.put("decision", decision);
        decidedVars.put("decision_detail", "approved".equals(decision)
                ? "The updated document/answer is now on file."
                : "No changes have been made — the previous document/answer stays on file.");
        // VCR.2 is one template shared by both outcomes — the tone override lets this send
        // show green ("ok") for an approval or red ("bad") for a rejection, instead of the
        // template's fixed neutral-blue default.
        workflowEmailClient.trigger("VCR.2", reg.getEmail(), decidedVars, "approved".equals(decision) ? "ok" : "bad");

        return true;
    }

    private void applyApprovedChange(SupplierChangeRequest cr) {
        SupplierRegistration reg = cr.getRegistration();
        switch (cr.getItemType()) {
            case "document" -> {
                SupplierRegistrationDocument doc = documentRepository
                        .findByRegistrationIdAndDocType(reg.getId(), cr.getItemKey())
                        .orElseGet(SupplierRegistrationDocument::new);
                doc.setRegistration(reg);
                doc.setDocType(cr.getItemKey());
                doc.setFileName(cr.getNewFileName());
                doc.setFolderItFileUid(cr.getNewFolderItFileUid());

                // Re-run OCR on the approved replacement and write the extracted values back onto
                // the registration's own flat fields (PAN/GST/certificates/etc.) — swapping only
                // the file and leaving the old number in place everywhere else would silently
                // defeat the entire point of "requesting a change". Left uncaught deliberately:
                // WorkFlow is known to redeliver this webhook, so letting a failure here roll back
                // the whole transaction (markDecided included) lets the retry try again cleanly,
                // rather than leaving the file swapped but the real fields stale.
                FolderItService.DownloadedFile downloaded;
                try {
                    downloaded = folderItService.downloadFileBytes(cr.getNewFolderItFileUid());
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Could not download the approved replacement document", e);
                }
                OpenAiVisionOcrService.ExtractResult extracted =
                        ocrService.extractFields(cr.getItemKey(), downloaded.bytes(), cr.getNewFileName());
                doc.setOcrExtractedFieldsJson(new JSONObject(extracted.values()).toString());
                applyExtractedFields(reg, extracted.values());
                registrationRepository.save(reg);

                documentRepository.save(doc);
            }
            case "attachment" -> {
                SupplierRegistrationAttachment att = attachmentRepository.findById(Long.parseLong(cr.getItemKey()))
                        .orElseThrow(() -> new RuntimeException("Attachment " + cr.getItemKey() + " no longer exists"));
                att.setFileName(cr.getNewFileName());
                att.setFolderItFileUid(cr.getNewFolderItFileUid());
                attachmentRepository.save(att);
            }
            case "answer" -> questionnaireService.updateSingleAnswer(
                    reg.getFormStudioResponseId(), Integer.parseInt(cr.getItemKey()), new JSONObject(cr.getNewAnswerJson()));
            default -> logger.warn("Unknown change request item type {} for change request {}", cr.getItemType(), cr.getId());
        }
    }

    /** Mirrors SupplierDocumentConfig's field keys to the matching flat column on
     *  SupplierRegistration — the same keys OCR extraction and the original upload flow already
     *  use, just applied here on approval of a document-replacement change request. Blank/missing
     *  values are left untouched rather than clearing an existing field the re-read simply missed. */
    private static void applyExtractedFields(SupplierRegistration reg, Map<String, String> values) {
        values.forEach((key, value) -> {
            if (value == null || value.isBlank()) return;
            switch (key) {
                case "cin" -> reg.setCinNumber(value);
                case "gstin" -> reg.setGstNumber(value);
                case "pan" -> reg.setPanNumber(value);
                case "benName" -> reg.setBeneficiaryName(value);
                case "acctNo" -> reg.setAccountNumber(value);
                case "ifsc" -> reg.setIfscCode(value);
                case "udyam" -> reg.setMsmeNumber(value);
                case "isoNo" -> reg.setIsoCertificateNo(value);
                case "isoBody" -> reg.setIsoCertifyingBody(value);
                case "isoExpiry" -> reg.setIsoExpiry(value);
                case "asNo" -> reg.setAs9100dCertificateNo(value);
                case "asBody" -> reg.setAs9100dCertifyingBody(value);
                case "asExpiry" -> reg.setAs9100dExpiry(value);
                case "nadcapNo" -> reg.setNadcapCertificateNo(value);
                case "nadcapExpiry" -> reg.setNadcapExpiry(value);
                default -> { /* no flat column for this key (e.g. NDA has none) */ }
            }
        });
    }

    /** A human-readable name for whichever document/attachment/answer a change request is about,
     *  for the VCR.1/VCR.2 email copy — not exposed via the API, only used to fill {{item_label}}. */
    private String itemLabel(SupplierChangeRequest cr) {
        try {
            return switch (cr.getItemType()) {
                case "document" -> SupplierDocumentConfig.byId(cr.getItemKey()).name();
                case "attachment" -> cr.getNewFileName() != null ? cr.getNewFileName() : "an additional document";
                case "answer" -> {
                    String prompt = questionnaireService.getQuestionPrompt(Integer.parseInt(cr.getItemKey()));
                    yield prompt != null ? prompt : "a questionnaire answer";
                }
                default -> "your submission";
            };
        } catch (Exception e) {
            return "your submission";
        }
    }

    private static String orDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    /** Same Java->WorkFlow trigger convention as SupplierRegistrationService's VO.2/VO.6 —
     *  never lets an email failure break the change-request flow itself. */
    private void triggerWorkflowEmail(String mailKey, String toEmail, Map<String, Object> variables) {
        workflowEmailClient.trigger(mailKey, toEmail, variables);
    }

    /** Full detail for the admin/employee reviewer — reason, old value, and either the proposed
     *  new file (preview URL) or the proposed new answer. */
    public ServiceResponse getForReview(Long changeRequestId) {
        ServiceResponse response = new ServiceResponse();
        SupplierChangeRequest cr = changeRequestRepository.findById(changeRequestId).orElse(null);
        if (cr == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Change request not found");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", cr.getId());
        data.put("registrationId", cr.getRegistration().getId());
        data.put("vendorName", cr.getRegistration().getVendorName());
        data.put("itemType", cr.getItemType());
        data.put("itemKey", cr.getItemKey());
        data.put("reason", cr.getReason());
        data.put("oldValueSummary", cr.getOldValueSummary());
        data.put("newFileName", cr.getNewFileName());
        data.put("newAnswerJson", cr.getNewAnswerJson());
        data.put("status", cr.getStatus());
        data.put("createdDate", cr.getCreatedDate());
        if (cr.getNewFolderItFileUid() != null) {
            data.put("newFilePreviewUrl", "/api/supplier-registration/change-request/" + cr.getId() + "/preview");
        }
        response.addData("result", data);
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Change request loaded");
    }

    public FolderItService.DownloadedFile getProposedFilePreview(Long changeRequestId) throws IOException {
        SupplierChangeRequest cr = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new RuntimeException("Change request not found"));
        if (cr.getNewFolderItFileUid() == null) {
            throw new RuntimeException("This change request has no proposed file");
        }
        return folderItService.downloadFileBytes(cr.getNewFolderItFileUid());
    }
}
