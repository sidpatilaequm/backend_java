package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.config.SupplierIntakeSeeder;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.SupplierDraftDTO;
import com.example.multimedia.file_upload_api.dto.VerifyResult;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.util.SupplierDocumentConfig;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.PasswordUtils;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SupplierRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierRegistrationService.class);

    private final SupplierRegistrationRepository registrationRepository;
    private final SupplierRegistrationDocumentRepository documentRepository;
    private final SupplierRegistrationAttachmentRepository attachmentRepository;
    private final SupplierChangeRequestRepository changeRequestRepository;
    private final FolderItService folderItService;
    private final OpenAiVisionOcrService ocrService;
    private final MicrovistaService microvistaService;
    private final UserDetailRepository userDetailRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final CompanyDetailsRepository companyDetailsRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AuthorizationRepository authorizationRepository;
    private final VendorMasterRepository vendorMasterRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final ServiceControllerUtils serviceControllerUtils;
    private final QuestionnaireService questionnaireService;
    private final VendorChangeRequestService vendorChangeRequestService;

    @Value("${workflow.api.base-url:http://localhost:8000}")
    private String workflowBaseUrl;

    @Value("${workflow.vendor-approval.id:8}")
    private Long vendorApprovalWorkflowId;

    @Value("${workflow.email-templates.service-token:}")
    private String workflowServiceToken;

    public SupplierRegistrationService(SupplierRegistrationRepository registrationRepository,
                                        SupplierRegistrationDocumentRepository documentRepository,
                                        SupplierRegistrationAttachmentRepository attachmentRepository,
                                        SupplierChangeRequestRepository changeRequestRepository,
                                        FolderItService folderItService,
                                        OpenAiVisionOcrService ocrService,
                                        MicrovistaService microvistaService,
                                        UserDetailRepository userDetailRepository,
                                        UserAuthenticationRepository userAuthenticationRepository,
                                        CompanyDetailsRepository companyDetailsRepository,
                                        SuperAdminRepository superAdminRepository,
                                        AuthorizationRepository authorizationRepository,
                                        VendorMasterRepository vendorMasterRepository,
                                        PasswordEncoder passwordEncoder,
                                        RestTemplate restTemplate,
                                        ServiceControllerUtils serviceControllerUtils,
                                        QuestionnaireService questionnaireService,
                                        VendorChangeRequestService vendorChangeRequestService) {
        this.registrationRepository = registrationRepository;
        this.documentRepository = documentRepository;
        this.attachmentRepository = attachmentRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.folderItService = folderItService;
        this.ocrService = ocrService;
        this.microvistaService = microvistaService;
        this.userDetailRepository = userDetailRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.companyDetailsRepository = companyDetailsRepository;
        this.superAdminRepository = superAdminRepository;
        this.authorizationRepository = authorizationRepository;
        this.vendorMasterRepository = vendorMasterRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.serviceControllerUtils = serviceControllerUtils;
        this.questionnaireService = questionnaireService;
        this.vendorChangeRequestService = vendorChangeRequestService;
    }

    // ── Document upload + OCR + FolderIt storage ────────────────────────────

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse uploadDocument(Long registrationId, String docType, MultipartFile file) {
        ServiceResponse response = new ServiceResponse();
        try {
            SupplierRegistration registration = registrationId != null
                    ? registrationRepository.findById(registrationId).orElseGet(this::newDraft)
                    : newDraft();
            if (registration.getId() == null) registration = registrationRepository.save(registration);

            // One FolderIt folder per application, created on first upload and reused for
            // every document after — named "{vendorCode} - {vendorName}" once approval
            // assigns a vendor code (see provisionVendorAccount), a registration-id
            // placeholder before that since neither is known yet at draft time.
            if (registration.getFolderitFolderUid() == null) {
                registration.setFolderitFolderUid(folderItService.getOrCreateVendorFolder("REG-" + registration.getId()));
                registration = registrationRepository.save(registration);
            }

            OpenAiVisionOcrService.ExtractResult extracted = ocrService.extractFields(docType, file);
            String folderItUid = folderItService.uploadFileToFolder(file, registration.getFolderitFolderUid());

            SupplierRegistrationDocument doc = documentRepository
                    .findByRegistrationIdAndDocType(registration.getId(), docType)
                    .orElseGet(SupplierRegistrationDocument::new);
            doc.setRegistration(registration);
            doc.setDocType(docType);
            doc.setFileName(file.getOriginalFilename());
            doc.setFolderItFileUid(folderItUid);
            doc.setOcrExtractedFieldsJson(new JSONObject(extracted.values()).toString());
            doc.setVerifyStatus("read");
            documentRepository.save(doc);

            Map<String, Object> data = new HashMap<>();
            data.put("registrationId", registration.getId());
            data.put("values", extracted.values());
            data.put("uncertain", extracted.uncertain());
            data.put("folderItFileUid", folderItUid);
            response.addData("result", data);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Document processed");
        } catch (IOException | RuntimeException e) {
            logger.error("Document upload failed for docType={}", docType, e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to process document: " + e.getMessage());
        }
    }

    // ── Extra attachments — no fixed slot, no OCR/verification ──────────────

    /**
     * Same folder/storage mechanics as uploadDocument, minus the OCR extraction and doc-type
     * slot — an application can carry any number of these, each its own row (not an upsert by
     * type like the fixed documents), for anything a supplier wants to attach that doesn't fit
     * one of the 7 defined document types.
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse uploadAttachment(Long registrationId, MultipartFile file) {
        ServiceResponse response = new ServiceResponse();
        try {
            SupplierRegistration registration = registrationId != null
                    ? registrationRepository.findById(registrationId).orElseGet(this::newDraft)
                    : newDraft();
            if (registration.getId() == null) registration = registrationRepository.save(registration);

            if (registration.getFolderitFolderUid() == null) {
                registration.setFolderitFolderUid(folderItService.getOrCreateVendorFolder("REG-" + registration.getId()));
                registration = registrationRepository.save(registration);
            }

            String folderItUid = folderItService.uploadFileToFolder(file, registration.getFolderitFolderUid());

            SupplierRegistrationAttachment attachment = new SupplierRegistrationAttachment();
            attachment.setRegistration(registration);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFolderItFileUid(folderItUid);
            attachment = attachmentRepository.save(attachment);

            Map<String, Object> data = new HashMap<>();
            data.put("registrationId", registration.getId());
            data.put("attachmentId", attachment.getId());
            data.put("fileName", attachment.getFileName());
            response.addData("result", data);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "File attached");
        } catch (IOException | RuntimeException e) {
            logger.error("Attachment upload failed", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Removes the DB reference only — the file itself stays in FolderIt, same as replacing one
     * of the fixed documents already does (see uploadDocument's upsert-by-type, which likewise
     * never deletes the FolderIt object it's replacing).
     */
    public ServiceResponse removeAttachment(Long attachmentId) {
        ServiceResponse response = new ServiceResponse();
        if (!attachmentRepository.existsById(attachmentId)) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Attachment not found");
        }
        attachmentRepository.deleteById(attachmentId);
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Attachment removed");
    }

    public FolderItService.DownloadedFile getAttachmentPreviewFile(Long attachmentId) throws java.io.IOException {
        SupplierRegistrationAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        return folderItService.downloadFileBytes(attachment.getFolderItFileUid());
    }

    /**
     * A registration can only be (re)edited/(re)submitted while it's an untouched DRAFT, or
     * after it's been REJECTED (the one status that's meant to send the applicant back to fix
     * something). Anything else — REGISTRATION_SUBMITTED, ESCALATED, ACTIVE, CANCELLED — means
     * it's already in flight or resolved, and the resume-code flow / saveDraft / submit must all
     * refuse to touch it instead of silently letting an applicant re-edit a live application.
     */
    private static final java.util.Set<String> EDITABLE_STATUSES = java.util.Set.of("DRAFT", "REJECTED");

    private SupplierRegistration newDraft() {
        SupplierRegistration reg = new SupplierRegistration();
        reg.setStatus("DRAFT");
        // Placeholder — must be filled in via saveDraft before submit; unique constraint
        // requires a value up front, so seed with a throwaway address tied to a random id.
        reg.setEmail("draft-" + UUID.randomUUID() + "@placeholder.local");
        return registrationRepository.save(reg);
    }

    // ── Verification ─────────────────────────────────────────────────────

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse verifyDocument(Long registrationId, String docType) {
        ServiceResponse response = new ServiceResponse();
        try {
            SupplierRegistrationDocument doc = documentRepository
                    .findByRegistrationIdAndDocType(registrationId, docType)
                    .orElseThrow(() -> new RuntimeException("No uploaded document found for " + docType));
            JSONObject values = new JSONObject(Optional.ofNullable(doc.getOcrExtractedFieldsJson()).orElse("{}"));
            SupplierDocumentConfig.DocDef def = SupplierDocumentConfig.byId(docType);

            VerifyResult result = switch (Optional.ofNullable(def.verifyKind()).orElse("")) {
                case "pan" -> microvistaService.verifyPan(values.optString("pan"));
                case "gstin" -> microvistaService.verifyGstin(values.optString("gstin"));
                case "cin" -> microvistaService.verifyCin(values.optString("cin"));
                case "udyam" -> microvistaService.verifyUdyam(values.optString("udyam"));
                case "bank" -> microvistaService.verifyBank(values.optString("acctNo"), values.optString("ifsc"), values.optString("benName"));
                default -> new VerifyResult(true, "No verification available for this document.", new ArrayList<>());
            };

            doc.setVerifyStatus(result.isVerified() ? "verified" : "error");
            doc.setVerifyDetailsJson(new JSONObject(Map.of("message", result.getMessage(), "details", result.getDetails())).toString());
            documentRepository.save(doc);

            response.addData("result", result);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Verification complete");
        } catch (Exception e) {
            logger.error("Verification failed for docType={}", docType, e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Verification failed: " + e.getMessage());
        }
    }

    // ── Draft save / resume ──────────────────────────────────────────────

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse saveDraft(SupplierDraftDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            SupplierRegistration reg = dto.getRegistrationId() != null
                    ? registrationRepository.findById(dto.getRegistrationId()).orElseGet(this::newDraft)
                    : newDraft();

            if (dto.getRegistrationId() != null && !EDITABLE_STATUSES.contains(reg.getStatus())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE,
                        "This application has already been submitted and can no longer be edited.");
            }

            reg.setVendorName(dto.getVendorName());
            reg.setAddress(dto.getAddress());

            reg.setContact1Name(dto.getContact1Name());
            reg.setContact1Role(dto.getContact1Role());
            reg.setContact1Email(dto.getContact1Email());
            reg.setContact1Phone(dto.getContact1Phone());
            reg.setContact2Name(dto.getContact2Name());
            reg.setContact2Role(dto.getContact2Role());
            reg.setContact2Email(dto.getContact2Email());
            reg.setContact2Phone(dto.getContact2Phone());
            reg.setPrimaryContact(dto.getPrimaryContact() != null ? dto.getPrimaryContact() : 1);
            reg.setBusinessTypes(dto.getBusinessTypes());
            reg.setBusinessScope(dto.getBusinessScope());
            reg.setCompanyType(dto.getCompanyType());
            reg.setTelephone(dto.getTelephone());
            reg.setFax(dto.getFax());
            reg.setWeeklyOff(dto.getWeeklyOff());
            reg.setAnnualTurnover(dto.getAnnualTurnover());
            reg.setTurnoverYear(dto.getTurnoverYear());
            reg.setRegulatoryActs(dto.getRegulatoryActs());
            reg.setManpowerOffice(dto.getManpowerOffice());
            reg.setManpowerSupervisor(dto.getManpowerSupervisor());
            reg.setManpowerWorkmen(dto.getManpowerWorkmen());
            reg.setShiftsPerDay(dto.getShiftsPerDay());
            reg.setSpareCapacity(dto.getSpareCapacity());
            reg.setFloorSpace(dto.getFloorSpace());
            reg.setEquipmentFacilities(dto.getEquipmentFacilities());
            reg.setDirectorsJson(dto.getDirectorsJson());
            reg.setMachineryJson(dto.getMachineryJson());
            reg.setDynamicAnswersJson(dto.getDynamicAnswersJson());
            reg.setDynamicQuestionnaireProcessId(dto.getDynamicQuestionnaireProcessId());
            reg.setDeclarationAccepted(Boolean.TRUE.equals(dto.getDeclarationAccepted()));

            // The resolved-primary mirror (contactName/designation/email/phone) is what the
            // rest of the system — unique constraint, resume-code email, WorkFlow submission,
            // post-approval login — reads. Only overwrite the unique `email` column when the
            // resolved primary contact actually has one, so a still-empty contact card doesn't
            // wipe out an email already saved on a previous draft save.
            boolean primaryIsTwo = Integer.valueOf(2).equals(reg.getPrimaryContact());
            String resolvedName = primaryIsTwo ? dto.getContact2Name() : dto.getContact1Name();
            String resolvedRole = primaryIsTwo ? dto.getContact2Role() : dto.getContact1Role();
            String resolvedEmail = primaryIsTwo ? dto.getContact2Email() : dto.getContact1Email();
            String resolvedPhone = primaryIsTwo ? dto.getContact2Phone() : dto.getContact1Phone();
            reg.setContactName(resolvedName);
            reg.setDesignation(resolvedRole);
            reg.setPhone(resolvedPhone);
            if (resolvedEmail != null && !resolvedEmail.isBlank()) reg.setEmail(resolvedEmail);

            reg.setGstNumber(dto.getGstNumber());
            reg.setPanNumber(dto.getPanNumber());
            reg.setMsmeNumber(dto.getMsmeNumber());
            reg.setCinNumber(dto.getCinNumber());
            reg.setBeneficiaryName(dto.getBeneficiaryName());
            reg.setAccountNumber(dto.getAccountNumber());
            reg.setIfscCode(dto.getIfscCode());
            reg.setBankName(dto.getBankName());
            reg.setIsoCertificateNo(dto.getIsoCertificateNo());
            reg.setIsoCertifyingBody(dto.getIsoCertifyingBody());
            reg.setIsoExpiry(dto.getIsoExpiry());
            reg.setAs9100dCertificateNo(dto.getAs9100dCertificateNo());
            reg.setAs9100dCertifyingBody(dto.getAs9100dCertifyingBody());
            reg.setAs9100dExpiry(dto.getAs9100dExpiry());
            reg.setNadcapCertificateNo(dto.getNadcapCertificateNo());
            reg.setNadcapExpiry(dto.getNadcapExpiry());

            if (reg.getResumeCode() == null) {
                reg.setResumeCode(generateResumeCode());
            }
            reg = registrationRepository.save(reg);

            // Fires on every deliberate "Save draft" click (explicitSave, set by the frontend
            // only for that button — never the silent ~3s background autosave, which would
            // otherwise mean an email every few seconds while someone's actively typing). Also
            // requires a real email to send to, since a draft can be explicitly saved before
            // one exists.
            if (hasRealEmail(reg) && Boolean.TRUE.equals(dto.getExplicitSave())) {
                sendResumeCodeEmail(reg);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("registrationId", reg.getId());
            data.put("resumeCode", reg.getResumeCode());
            response.addData("result", data);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Draft saved");
        } catch (Exception e) {
            logger.error("Save draft failed", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to save draft: " + e.getMessage());
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean hasRealEmail(SupplierRegistration reg) {
        return !isBlank(reg.getEmail()) && !reg.getEmail().contains("@placeholder.local");
    }

    /**
     * A null value silently drops the key when building a JSONObject (from a Map, via .put()),
     * so a blank vendor/contact name would otherwise leave a raw {{merge_tag}} unresolved in the
     * rendered email instead of falling back to nothing to substitute. Used for anything handed
     * to WorkFlow — draft-save/approval trigger variables here, and submit()'s request_metadata.
     */
    private static String orDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String generateResumeCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (registrationRepository.findByResumeCode(code).isPresent());
        return code;
    }

    /**
     * total/answered across everything a draft needs before it could be submitted (required
     * documents, contact fields, declaration, and every questionnaire question); requiredLeft is
     * the subset of what's missing that actually blocks submission (docs/contact/declaration are
     * always required; questionnaire questions only count here if marked mandatory);
     * incompleteCategories names which parts still need attention — "Documents"/"Contact
     * details"/"Declaration" plus the questionnaire's own section names — for the draft-saved
     * email's "Answered X of Y" / "Required left" / "Still to complete" rows. Used only for that
     * email; submit() has its own independent, authoritative gate that actually blocks submission.
     */
    private record ChecklistProgress(int total, int answered, int requiredLeft, List<String> incompleteCategories) {}

    private ChecklistProgress buildChecklistProgress(SupplierRegistration reg) {
        int total = 0;
        int answered = 0;
        int requiredLeft = 0;
        List<String> incompleteCategories = new ArrayList<>();

        int docsMissing = 0;
        for (SupplierDocumentConfig.DocDef d : SupplierDocumentConfig.DOCS) {
            if (!d.required()) continue;
            total++;
            boolean present = documentRepository.findByRegistrationIdAndDocType(reg.getId(), d.id())
                    .map(doc -> doc.getFolderItFileUid() != null).orElse(false);
            if (present) answered++; else docsMissing++;
        }
        if (docsMissing > 0) { requiredLeft += docsMissing; incompleteCategories.add("Documents"); }

        int contactMissing = 0;
        for (String field : new String[]{reg.getContact1Name(), reg.getContact1Role(), reg.getContact1Phone()}) {
            total++;
            if (isBlank(field)) contactMissing++; else answered++;
        }
        if (contactMissing > 0) { requiredLeft += contactMissing; incompleteCategories.add("Contact details"); }

        total++;
        if (Boolean.TRUE.equals(reg.getDeclarationAccepted())) {
            answered++;
        } else {
            requiredLeft++;
            incompleteCategories.add("Declaration");
        }

        QuestionnaireService.QuestionnaireProgress qp = questionnaireService.countQuestionnaireProgress(
                reg.getDynamicAnswersJson(), reg.getDynamicQuestionnaireProcessId());
        total += qp.total();
        answered += qp.total() - qp.unanswered();
        requiredLeft += qp.unansweredRequired();
        incompleteCategories.addAll(qp.incompleteSectionNames());

        return new ChecklistProgress(total, answered, requiredLeft, incompleteCategories);
    }

    /**
     * Renders and sends one of the 3 live-triggered admin-editable templates
     * (email_templates table, WorkFlow-side) instead of building the email
     * ourselves — see WorkFlow's services/email_templates.py. Guarded by a
     * shared-secret header rather than any user session, since this is a
     * server-to-server call with no logged-in WorkFlow user on this side.
     */
    private void triggerWorkflowEmail(String mailKey, String toEmail, Map<String, Object> variables) {
        if (workflowServiceToken == null || workflowServiceToken.isBlank()) {
            logger.warn("Skipping {} email — workflow.email-templates.service-token not configured", mailKey);
            return;
        }
        if (toEmail == null || toEmail.isBlank() || toEmail.contains("@placeholder.local")) {
            logger.warn("Skipping {} email — no real recipient email yet", mailKey);
            return;
        }
        try {
            JSONObject payload = new JSONObject()
                    .put("to_email", toEmail)
                    .put("variables", new JSONObject(variables));
            String url = workflowBaseUrl + "/api/email-templates/trigger/" + mailKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Token", workflowServiceToken);
            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            logger.error("Failed to trigger {} email for {}", mailKey, toEmail, e);
        }
    }

    private void sendResumeCodeEmail(SupplierRegistration reg) {
        ChecklistProgress progress = buildChecklistProgress(reg);
        Map<String, Object> variables = new HashMap<>();
        variables.put("contact_name", orDefault(reg.getContactName(), "there"));
        variables.put("vendor_name", orDefault(reg.getVendorName(), "your company"));
        variables.put("resume_code", reg.getResumeCode());
        variables.put("answered_progress", progress.answered() + " of " + progress.total());
        variables.put("required_left", String.valueOf(progress.requiredLeft()));
        variables.put("still_to_complete", progress.incompleteCategories().isEmpty()
                ? "Nothing — you're ready to submit."
                : String.join(", ", progress.incompleteCategories()));
        triggerWorkflowEmail("VO.2", reg.getEmail(), variables);
    }

    public ServiceResponse getDraftByCode(String code) {
        ServiceResponse response = new ServiceResponse();
        SupplierRegistration reg = registrationRepository.findByResumeCode(code).orElse(null);
        if (reg == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "No draft found for that code");
        }
        List<SupplierRegistrationDocument> docs = documentRepository.findByRegistrationId(reg.getId());
        docs.sort(Comparator.comparingInt(d -> SupplierDocumentConfig.orderIndex(d.getDocType())));
        Map<String, Object> data = new HashMap<>();
        data.put("registration", reg);
        List<Map<String, Object>> docsOut = new ArrayList<>();
        for (SupplierRegistrationDocument d : docs) {
            Map<String, Object> docOut = new HashMap<>();
            docOut.put("docType", d.getDocType());
            docOut.put("fileName", d.getFileName());
            docOut.put("folderItFileUid", d.getFolderItFileUid());
            docOut.put("values", new JSONObject(Optional.ofNullable(d.getOcrExtractedFieldsJson()).orElse("{}")).toMap());
            docOut.put("verifyStatus", d.getVerifyStatus());
            docsOut.add(docOut);
        }
        data.put("documents", docsOut);
        // No previewUrl here — matches the existing fixed documents on this same public,
        // unauthenticated resume path: the preview endpoint requires the employee/admin JWT
        // (see getRegistrationForReview below), which an anonymous applicant never has.
        data.put("attachments", buildAttachmentsOut(reg.getId(), false));
        response.addData("result", data);
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Draft loaded");
    }

    /** {id, fileName, uploadedAt, previewUrl?} per extra file — shared by getDraftByCode/getRegistrationForReview. */
    private List<Map<String, Object>> buildAttachmentsOut(Long registrationId, boolean includePreviewUrl) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SupplierRegistrationAttachment a : attachmentRepository.findByRegistrationIdOrderByCreatedDateAsc(registrationId)) {
            Map<String, Object> attOut = new HashMap<>();
            attOut.put("id", a.getId());
            attOut.put("fileName", a.getFileName());
            attOut.put("uploadedAt", a.getCreatedDate());
            if (includePreviewUrl) attOut.put("previewUrl", "/api/supplier-registration/attachment/" + a.getId() + "/preview");
            out.add(attOut);
        }
        return out;
    }

    /**
     * Authenticated view for whoever is reviewing a submitted application (an approver
     * acting on the WorkFlow request) — same shape as getDraftByCode, but looked up by
     * registration id directly (reviewers don't have the applicant's resume code) and with
     * a fresh, short-lived download link per document so the actual certificate/cheque/etc.
     * can be opened, plus each document's verification details.
     */
    public ServiceResponse getRegistrationForReview(Long registrationId) {
        ServiceResponse response = new ServiceResponse();
        SupplierRegistration reg = registrationRepository.findById(registrationId).orElse(null);
        if (reg == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Registration not found");
        }
        response.addData("result", buildRegistrationDetail(reg));
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Registration loaded");
    }

    /**
     * Admin-facing "Approved Suppliers" list — every registration that made it through the
     * Become-a-Supplier flow to ACTIVE, with the classification an approver set (see
     * setVendorCategory) alongside it. Deliberately reads supplier_registration directly rather
     * than VendorController's /api/vendors/all, which is backed by the unrelated legacy
     * vendor_master table (confirmed empty of any vendor provisioned through this flow).
     */
    public ServiceResponse listApprovedSuppliers() {
        ServiceResponse response = new ServiceResponse();
        List<Map<String, Object>> out = new ArrayList<>();
        for (SupplierRegistration reg : registrationRepository.findByStatusOrderByApprovedDateDesc("ACTIVE")) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", reg.getId());
            row.put("vendorName", reg.getVendorName());
            row.put("vendorCode", reg.getVendorCode());
            row.put("email", reg.getEmail());
            row.put("phone", reg.getPhone());
            row.put("address", reg.getAddress());
            row.put("gstNumber", reg.getGstNumber());
            row.put("panNumber", reg.getPanNumber());
            row.put("companyType", reg.getCompanyType());
            row.put("businessTypes", reg.getBusinessTypes());
            row.put("vendorCategory", reg.getVendorCategory());
            row.put("approvedBy", reg.getApprovedBy());
            row.put("approvedDate", reg.getApprovedDate());
            out.add(row);
        }
        response.addData("suppliers", out);
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Approved suppliers retrieved");
    }

    private static final java.util.Set<String> VENDOR_CATEGORIES =
            java.util.Set.of("PRODUCT", "SERVICE", "SCHEDULING_AGREEMENT", "SUBCONTRACTING");

    /**
     * The deciding approver's classification pick (Product / Service / Scheduling agreement /
     * Sub-contracting) — only the first approver to call this actually sets it (see
     * SupplierRegistrationRepository.claimVendorCategory); returns whichever value actually won,
     * so a caller whose own pick lost a close race still gets back the truth rather than assuming
     * their own value stuck.
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse setVendorCategory(Long registrationId, String category) {
        ServiceResponse response = new ServiceResponse();
        String normalized = category == null ? null : category.trim().toUpperCase();
        if (normalized == null || !VENDOR_CATEGORIES.contains(normalized)) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE,
                    "category must be one of " + VENDOR_CATEGORIES);
        }
        SupplierRegistration reg = registrationRepository.findById(registrationId).orElse(null);
        if (reg == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Registration not found");
        }

        int claimed = registrationRepository.claimVendorCategory(registrationId, normalized);
        // Re-fetch regardless of who won — a concurrent caller may have already claimed it, and
        // this caller needs the value that actually stuck either way, not just its own guess.
        String finalCategory = registrationRepository.findById(registrationId)
                .map(SupplierRegistration::getVendorCategory).orElse(normalized);

        Map<String, Object> data = new HashMap<>();
        data.put("vendorCategory", finalCategory);
        data.put("decidedByYou", claimed > 0);
        response.addData("result", data);
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Classification recorded");
    }

    /**
     * Same shape as getRegistrationForReview, for the approved vendor themselves rather than an
     * admin/employee reviewer — resolved from their own JWT login email (SupplierRegistration.email
     * is unique and is exactly the address provisionVendorAccount created their login under), not
     * a caller-supplied id, so a vendor can never end up looking at another vendor's profile.
     * Also includes their own change requests (see VendorChangeRequestService) so the frontend can
     * show a "pending" badge next to whichever document/answer already has one outstanding.
     */
    public ServiceResponse getMyProfile(String email) {
        ServiceResponse response = new ServiceResponse();
        SupplierRegistration reg = registrationRepository.findByEmail(email).orElse(null);
        if (reg == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "No supplier profile found for this account");
        }
        Map<String, Object> data = buildRegistrationDetail(reg);
        List<Map<String, Object>> changeRequestsOut = new ArrayList<>();
        for (SupplierChangeRequest cr : changeRequestRepository.findByRegistrationIdOrderByCreatedDateDesc(reg.getId())) {
            Map<String, Object> crOut = new HashMap<>();
            crOut.put("id", cr.getId());
            crOut.put("itemType", cr.getItemType());
            crOut.put("itemKey", cr.getItemKey());
            crOut.put("reason", cr.getReason());
            crOut.put("status", cr.getStatus());
            crOut.put("createdDate", cr.getCreatedDate());
            changeRequestsOut.add(crOut);
        }
        data.put("changeRequests", changeRequestsOut);
        response.addData("result", data);
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Profile loaded");
    }

    private Map<String, Object> buildRegistrationDetail(SupplierRegistration reg) {
        List<SupplierRegistrationDocument> docs = documentRepository.findByRegistrationId(reg.getId());
        docs.sort(Comparator.comparingInt(d -> SupplierDocumentConfig.orderIndex(d.getDocType())));
        Map<String, Object> data = new HashMap<>();
        data.put("registration", reg);
        List<Map<String, Object>> docsOut = new ArrayList<>();
        for (SupplierRegistrationDocument d : docs) {
            SupplierDocumentConfig.DocDef def = SupplierDocumentConfig.byId(d.getDocType());
            JSONObject values = new JSONObject(Optional.ofNullable(d.getOcrExtractedFieldsJson()).orElse("{}"));
            Map<String, Object> docOut = new HashMap<>();
            docOut.put("id", d.getId());
            docOut.put("docType", d.getDocType());
            docOut.put("docName", def.name());
            docOut.put("fileName", d.getFileName());
            docOut.put("uploadedAt", d.getCreatedDate());
            // Labeled per SupplierDocumentConfig's field defs (same labels the supplier's
            // own upload screen uses) instead of dumping raw OCR keys at the reviewer.
            List<Map<String, Object>> fieldsOut = new ArrayList<>();
            for (SupplierDocumentConfig.FieldDef fd : def.fields()) {
                Map<String, Object> f = new HashMap<>();
                f.put("key", fd.key());
                f.put("label", fd.label());
                f.put("value", values.optString(fd.key(), ""));
                fieldsOut.add(f);
            }
            docOut.put("fields", fieldsOut);
            docOut.put("verifyStatus", d.getVerifyStatus());
            docOut.put("verifyKind", def.verifyKind());
            if (d.getVerifyDetailsJson() != null) {
                JSONObject vd = new JSONObject(d.getVerifyDetailsJson());
                docOut.put("verifyMessage", vd.optString("message", null));
                docOut.put("verifyDetails", vd.optJSONArray("details") != null ? vd.getJSONArray("details").toList() : List.of());
            }
            if (d.getFolderItFileUid() != null) {
                docOut.put("previewUrl", "/api/supplier-registration/document/" + d.getId() + "/preview");
            }
            docsOut.add(docOut);
        }
        data.put("documents", docsOut);
        data.put("attachments", buildAttachmentsOut(reg.getId(), true));
        data.put("dynamicAnswers", questionnaireService.getAnswersForReview(reg.getFormStudioResponseId()).toList());
        return data;
    }

    // Streams the document bytes through our own server (rather than redirecting to FolderIt's
    // presigned URL directly) so the reviewer's browser gets an inline-viewable response instead
    // of FolderIt's forced-download disposition, and so this stays behind our own JWT auth.
    public FolderItService.DownloadedFile getDocumentPreviewFile(Long docId) throws java.io.IOException {
        SupplierRegistrationDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (doc.getFolderItFileUid() == null) {
            throw new RuntimeException("No file uploaded for this document");
        }
        return folderItService.downloadFileBytes(doc.getFolderItFileUid());
    }

    // ── Submit into WorkFlow's "Vendor Approval" workflow ────────────────

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse submit(Long registrationId) {
        ServiceResponse response = new ServiceResponse();
        try {
            SupplierRegistration reg = registrationRepository.findById(registrationId)
                    .orElseThrow(() -> new RuntimeException("Registration not found"));

            if (!EDITABLE_STATUSES.contains(reg.getStatus())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE,
                        "This application has already been submitted for approval.");
            }

            List<String> missing = new ArrayList<>();
            for (SupplierDocumentConfig.DocDef d : SupplierDocumentConfig.DOCS) {
                if (!d.required()) continue;
                boolean present = documentRepository.findByRegistrationIdAndDocType(reg.getId(), d.id())
                        .map(doc -> doc.getFolderItFileUid() != null).orElse(false);
                if (!present) missing.add(d.name());
            }

            if (isBlank(reg.getContact1Name())) missing.add("contact 1 name");
            if (isBlank(reg.getContact1Role())) missing.add("contact 1 designation");
            if (isBlank(reg.getContact1Email())) missing.add("contact 1 email");
            if (isBlank(reg.getContact1Phone())) missing.add("contact 1 phone");
            // A second contact is optional overall, but once any part of it is filled in,
            // all of it is required — mirrors the original prototype's readiness check.
            boolean hasSecondContact = !isBlank(reg.getContact2Name()) || !isBlank(reg.getContact2Email())
                    || !isBlank(reg.getContact2Role()) || !isBlank(reg.getContact2Phone());
            if (hasSecondContact) {
                if (isBlank(reg.getContact2Name())) missing.add("contact 2 name");
                if (isBlank(reg.getContact2Role())) missing.add("contact 2 designation");
                if (isBlank(reg.getContact2Email())) missing.add("contact 2 email");
                if (isBlank(reg.getContact2Phone())) missing.add("contact 2 phone");
            }
            if (!Boolean.TRUE.equals(reg.getDeclarationAccepted())) missing.add("the declaration");
            if (reg.getEmail() == null || reg.getEmail().contains("@placeholder.local")) missing.add(0, "a real contact email");
            if (!missing.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE,
                        "Missing required: " + String.join(", ", missing));
            }

            // Legal name comes off the cancelled cheque's verified account-holder name, same as
            // the original prototype — there's no separate "company name" field to type.
            if (isBlank(reg.getVendorName()) && !isBlank(reg.getBeneficiaryName())) {
                reg.setVendorName(reg.getBeneficiaryName());
            }

            // Admin-defined questionnaire (if any is published + active) — validated and copied
            // into Form Studio's own responses/answers/answer_options tables here, not called
            // through Form Studio's API, so submission never depends on it being up. No-op if
            // no questionnaire is currently active.
            try {
                Integer responseId = questionnaireService.validateAndPersistAnswers(
                        reg.getDynamicAnswersJson(), reg.getContactName(), reg.getEmail());
                if (responseId != null) reg.setFormStudioResponseId(responseId);
            } catch (QuestionnaireService.ValidationException e) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE,
                        "Missing required: " + e.getMessage());
            }

            UserDetail intakeUser = userDetailRepository.findByEmail(SupplierIntakeSeeder.INTAKE_EMAIL)
                    .orElseThrow(() -> new RuntimeException("Intake service account not found"));

            JSONObject metadata = new JSONObject()
                    .put("registrationId", reg.getId())
                    .put("vendorName", orDefault(reg.getVendorName(), "your company"))
                    .put("contactName", orDefault(reg.getContactName(), "there"))
                    .put("email", reg.getEmail())
                    .put("phone", reg.getPhone())
                    .put("gstNumber", reg.getGstNumber())
                    .put("panNumber", reg.getPanNumber())
                    .put("contact2Name", reg.getContact2Name())
                    .put("contact2Email", reg.getContact2Email());

            JSONObject payload = new JSONObject()
                    .put("title", "Supplier registration — " + reg.getVendorName())
                    .put("description", "Self-serve Become-a-Supplier submission")
                    .put("request_type", "vendor_registration")
                    .put("department", JSONObject.NULL)
                    .put("workflow_id", vendorApprovalWorkflowId)
                    .put("request_metadata", metadata);

            String url = UriComponentsBuilder.fromHttpUrl(workflowBaseUrl + "/api/requests/")
                    .queryParam("user_id", intakeUser.getUserId())
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            JSONObject wfResponse = new JSONObject(restTemplate.postForObject(url, request, String.class));

            reg.setStatus("REGISTRATION_SUBMITTED");
            reg.setWorkflowRequestId(wfResponse.getLong("id"));
            registrationRepository.save(reg);

            Map<String, Object> data = new HashMap<>();
            data.put("registrationId", reg.getId());
            data.put("workflowRequestId", reg.getWorkflowRequestId());
            response.addData("result", data);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Submitted for review");
        } catch (Exception e) {
            logger.error("Submit failed for registrationId={}", registrationId, e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to submit: " + e.getMessage());
        }
    }

    // ── Approval webhook (WorkFlow's fire_webhook -> here) ───────────────

    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalWebhook(JSONObject payload) {
        String event = payload.optString("event");
        JSONObject requestObj = payload.optJSONObject("request");
        if (requestObj == null) return;
        long workflowRequestId = requestObj.optLong("id", -1);
        if (workflowRequestId < 0) return;

        // Vendor change requests (workflow id 13) share this same webhook URL/secret as the
        // original vendor-approval workflow (id 8) — check that table first since its
        // workflowRequestId lives in a separate row than SupplierRegistration's own.
        if (vendorChangeRequestService.handleWebhookIfChangeRequest(workflowRequestId, event)) return;

        SupplierRegistration reg = registrationRepository.findByWorkflowRequestId(workflowRequestId).orElse(null);
        if (reg == null) {
            logger.warn("No supplier_registration found for workflow_request_id={}", workflowRequestId);
            return;
        }

        if ("request.approved".equals(event)) {
            if (reg.getUserId() != null) {
                logger.info("Registration {} already provisioned (userId={}) — ignoring duplicate approval webhook", reg.getId(), reg.getUserId());
                return;
            }
            provisionVendorAccount(reg);
        } else if ("request.rejected".equals(event)) {
            reg.setStatus("REJECTED");
            registrationRepository.save(reg);
        } else if ("request.escalated".equals(event)) {
            // The workflow needs manual attention (SLA breach under an escalate rejection
            // policy) rather than a clean approve/reject — reflect that instead of leaving
            // the registration stuck looking "still submitted" with no path forward.
            reg.setStatus("ESCALATED");
            registrationRepository.save(reg);
        } else if ("request.cancelled".equals(event)) {
            reg.setStatus("CANCELLED");
            registrationRepository.save(reg);
        }
    }

    private void provisionVendorAccount(SupplierRegistration reg) {
        SuperAdmin systemAdmin = superAdminRepository.findByEmail("system@internal")
                .orElseThrow(() -> new RuntimeException("System admin not found"));
        Authorization vendorAuth = authorizationRepository.findByAuthKeyIgnoreCase("vendor")
                .orElseThrow(() -> new RuntimeException("Vendor role authorization not found"));

        String rawPassword = PasswordUtils.generateRandomPassword(reg.getContactName());
        String vendorCode = "VEND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        UserDetail user = new UserDetail();
        user.setSuperAdmin(systemAdmin);
        user.setEmail(reg.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFirstName(reg.getContactName());
        user.setPhoneNumber(reg.getPhone());
        user.setDesignation(reg.getDesignation());
        user.setUserType(UserType.VENDOR);
        user.setIsActive(true);
        user = userDetailRepository.save(user);

        UserAuthentication auth = new UserAuthentication();
        auth.setUserId(user.getUserId());
        auth.setAuthKey(String.valueOf(vendorAuth.getAuthId()));
        auth.setIsActive(true);
        userAuthenticationRepository.save(auth);

        CompanyDetails company = new CompanyDetails();
        company.setCompanyName(reg.getVendorName());
        company.setLegalTradeName(reg.getVendorName());
        company.setRegisteredAddress(reg.getAddress());
        company.setGstinNumber(reg.getGstNumber());
        company.setPanNumber(reg.getPanNumber());
        company.setSuperAdmin(systemAdmin);
        company.setUser(user);
        company.setAuthKey("vendor");
        company.setStatus("ACTIVE");
        company.setCompanyCode(vendorCode);
        company = companyDetailsRepository.save(company);

        user.setCompany(company);
        userDetailRepository.save(user);

        // Also register the vendor in vendor_master — the legacy/SAP-style table RFQ vendor
        // assignment (WorkFlow's /api/vendor/selection-list) and vendor-facing Gate Entry
        // (GateEntryServiceImpl.getVendorGateStatus, which 500s without a matching row) actually
        // read from, independently of supplier_registration/CompanyDetails above. Guarded by
        // email so a redelivered approval webhook can't create a duplicate row.
        if (!vendorMasterRepository.existsByEmail(reg.getEmail())) {
            VendorMaster vendorMaster = new VendorMaster();
            vendorMaster.setBpNo(vendorCode);
            vendorMaster.setName(reg.getVendorName());
            vendorMaster.setEmail(reg.getEmail());
            vendorMaster.setGstNumber(reg.getGstNumber());
            vendorMaster.setPan(reg.getPanNumber());
            vendorMaster.setCompanyCode(vendorCode);
            vendorMaster.setBankAccountNumber(reg.getAccountNumber());
            vendorMaster.setSuperAdmin(systemAdmin);
            vendorMasterRepository.save(vendorMaster);
        }

        reg.setStatus("ACTIVE");
        reg.setVendorCode(vendorCode);
        reg.setUserId(user.getUserId());
        reg.setCompanyId(company.getCompanyId());
        reg.setApprovedDate(LocalDateTime.now());
        registrationRepository.save(reg);

        if (reg.getFolderitFolderUid() != null) {
            try {
                folderItService.renameFolder(reg.getFolderitFolderUid(), vendorCode + " - " + reg.getVendorName());
            } catch (Exception e) {
                // Cosmetic — the documents are already safely stored under the placeholder
                // name, so a rename hiccup shouldn't block the vendor actually being approved.
                logger.warn("Could not rename FolderIt folder {} for registration {}", reg.getFolderitFolderUid(), reg.getId(), e);
            }
        }

        sendCredentialsEmail(reg, rawPassword);
    }

    private void sendCredentialsEmail(SupplierRegistration reg, String rawPassword) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("contact_name", orDefault(reg.getContactName(), "there"));
        variables.put("vendor_name", orDefault(reg.getVendorName(), "your company"));
        variables.put("vendor_code", reg.getVendorCode());
        variables.put("login_email", reg.getEmail());
        variables.put("password", rawPassword);
        triggerWorkflowEmail("VO.6", reg.getEmail(), variables);
    }
}
