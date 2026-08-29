package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.SupplierDraftDTO;
import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.QuestionnaireService;
import com.example.multimedia.file_upload_api.service.SupplierRegistrationService;
import com.example.multimedia.file_upload_api.service.VendorChangeRequestService;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@RestController
public class SupplierRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierRegistrationController.class);

    private final SupplierRegistrationService service;
    private final QuestionnaireService questionnaireService;
    private final VendorChangeRequestService vendorChangeRequestService;
    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final AuthorizationRepository authorizationRepository;

    @Value("${workflow.webhook.secret:}")
    private String webhookSecret;

    public SupplierRegistrationController(SupplierRegistrationService service,
                                           QuestionnaireService questionnaireService,
                                           VendorChangeRequestService vendorChangeRequestService,
                                           SuperAdminRepository superAdminRepository,
                                           UserDetailRepository userDetailRepository,
                                           UserAuthenticationRepository userAuthenticationRepository,
                                           AuthorizationRepository authorizationRepository) {
        this.service = service;
        this.questionnaireService = questionnaireService;
        this.vendorChangeRequestService = vendorChangeRequestService;
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.authorizationRepository = authorizationRepository;
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        String email = auth.getName();

        if (superAdminRepository.existsByEmail(email)) return true;

        java.util.Optional<UserDetail> userOpt = userDetailRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        java.util.Optional<UserAuthentication> authOpt = userAuthenticationRepository.findByUserId(userOpt.get().getUserId());
        if (authOpt.isEmpty()) return false;

        String authKey = authOpt.get().getAuthKey();
        java.util.Optional<Authorization> authorization;
        try {
            authorization = authorizationRepository.findById(Integer.parseInt(authKey));
        } catch (NumberFormatException e) {
            authorization = authorizationRepository.findByAuthKeyIgnoreCase(authKey);
        }
        if (authorization.isEmpty()) return false;

        String roleName = authorization.get().getAuthName().toUpperCase();
        return !roleName.equals("VENDOR") && !roleName.equals("EMPLOYEE") && !roleName.equals("PURCHASE_DEPT");
    }

    private static String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    /**
     * The admin-defined questionnaire (if any process is published + marked active for this
     * integration) for the "What no document tells us" section — public/unauthenticated like the
     * rest of the applicant-facing endpoints. Reads the shared DB tables directly (QuestionnaireService),
     * not Form Studio's API, so this doesn't depend on Form Studio being up.
     */
    @GetMapping("/api/public/supplier-registration/questionnaire")
    public ResponseEntity<String> getQuestionnaire() {
        JSONObject questionnaire = questionnaireService.getActiveQuestionnaire();
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body((questionnaire != null ? questionnaire : new JSONObject()).toString());
    }

    @PostMapping("/api/public/supplier-registration/documents/{docType}")
    public ResponseEntity<ServiceResponse> uploadDocument(
            @PathVariable String docType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "registrationId", required = false) Long registrationId) {
        ServiceResponse response = service.uploadDocument(registrationId, docType, file);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    /** The file behind one dynamic file_upload question's answer — see uploadQuestionFile. */
    @PostMapping("/api/public/supplier-registration/dynamic-question-file/{questionId}")
    public ResponseEntity<ServiceResponse> uploadQuestionFile(
            @PathVariable Integer questionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "registrationId", required = false) Long registrationId) {
        ServiceResponse response = service.uploadQuestionFile(registrationId, questionId, file);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    /** Extra files with no fixed doc type — no OCR, otherwise the same storage flow as uploadDocument. */
    @PostMapping("/api/public/supplier-registration/attachments")
    public ResponseEntity<ServiceResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "registrationId", required = false) Long registrationId) {
        ServiceResponse response = service.uploadAttachment(registrationId, file);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/public/supplier-registration/attachments/{attachmentId}")
    public ResponseEntity<ServiceResponse> removeAttachment(@PathVariable Long attachmentId) {
        ServiceResponse response = service.removeAttachment(attachmentId);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.status(404).body(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/public/supplier-registration/verify")
    public ResponseEntity<ServiceResponse> verify(
            @RequestParam("registrationId") Long registrationId,
            @RequestParam("docType") String docType) {
        ServiceResponse response = service.verifyDocument(registrationId, docType);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/public/supplier-registration/draft")
    public ResponseEntity<ServiceResponse> saveDraft(@RequestBody SupplierDraftDTO dto) {
        ServiceResponse response = service.saveDraft(dto);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/public/supplier-registration/draft/{code}")
    public ResponseEntity<ServiceResponse> getDraft(@PathVariable String code) {
        ServiceResponse response = service.getDraftByCode(code);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.status(404).body(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/public/supplier-registration/submit")
    public ResponseEntity<ServiceResponse> submit(@RequestParam("registrationId") Long registrationId) {
        ServiceResponse response = service.submit(registrationId);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    /**
     * Deliberately NOT under /api/public/** — this is the approver-facing view (documents +
     * every extracted/verified field) for a submitted application, reached from the Workflow
     * tab's request-details modal via the registrationId carried in request_metadata. Requires
     * the normal employee/admin JWT auth, unlike the applicant-facing endpoints above.
     */
    @GetMapping("/api/supplier-registration/{registrationId}")
    public ResponseEntity<ServiceResponse> getForReview(@PathVariable Long registrationId) {
        ServiceResponse response = service.getRegistrationForReview(registrationId);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.status(404).body(response);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin-facing list of every Become-a-Supplier registration that reached ACTIVE, with its
     * classification — backs a dedicated "Approved Suppliers" screen (VendorController's
     * /api/vendors/all is a different, legacy list unrelated to this flow).
     */
    @GetMapping("/api/supplier-registration/approved-suppliers")
    public ResponseEntity<ServiceResponse> listApprovedSuppliers() {
        if (!isAdmin()) return ResponseEntity.status(403).body(null);
        return ResponseEntity.ok(service.listApprovedSuppliers());
    }

    /**
     * The deciding approver's Product/Service/Scheduling agreement/Sub-contracting pick(s) — call
     * this right before the actual WorkFlow approval action. Only the first approver to call it
     * actually sets the value (see SupplierRegistrationService.setVendorCategory); the frontend
     * shows the picker only when getForReview's registration.vendorCategory is still null.
     */
    @PostMapping("/api/supplier-registration/{registrationId}/classification")
    public ResponseEntity<ServiceResponse> setVendorCategory(@PathVariable Long registrationId, @RequestBody java.util.Map<String, Object> body) {
        Object raw = body.get("categories");
        java.util.List<String> categories = raw instanceof java.util.List<?> list
                ? list.stream().map(String::valueOf).toList()
                : java.util.List.of();
        ServiceResponse response = service.setVendorCategory(registrationId, categories);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    /**
     * Streams a document's bytes through our own server with an inline Content-Disposition, so
     * clicking "View" in the reviewer UI renders the PDF/image instead of forcing a download —
     * FolderIt's own presigned link bakes in "attachment". Also outside /api/public/**.
     */
    @GetMapping("/api/supplier-registration/document/{docId}/preview")
    public ResponseEntity<byte[]> previewDocument(@PathVariable Long docId) {
        try {
            com.example.multimedia.file_upload_api.service.FolderItService.DownloadedFile file =
                    service.getDocumentPreviewFile(docId);
            return ResponseEntity.ok()
                    .header("Content-Type", file.contentType())
                    .header("Content-Disposition", "inline")
                    .header("Cache-Control", "no-store")
                    .body(file.bytes());
        } catch (Exception e) {
            logger.warn("Could not fetch preview for document {}", docId, e);
            return ResponseEntity.status(404).build();
        }
    }

    /** Same as previewDocument above, for the free-form extra attachments. */
    @GetMapping("/api/supplier-registration/attachment/{attachmentId}/preview")
    public ResponseEntity<byte[]> previewAttachment(@PathVariable Long attachmentId) {
        try {
            com.example.multimedia.file_upload_api.service.FolderItService.DownloadedFile file =
                    service.getAttachmentPreviewFile(attachmentId);
            return ResponseEntity.ok()
                    .header("Content-Type", file.contentType())
                    .header("Content-Disposition", "inline")
                    .header("Cache-Control", "no-store")
                    .body(file.bytes());
        } catch (Exception e) {
            logger.warn("Could not fetch preview for attachment {}", attachmentId, e);
            return ResponseEntity.status(404).build();
        }
    }

    // ── Approved vendor self-service: view own profile, request a change ────

    /**
     * The logged-in vendor's own registration — documents, attachments, dynamic-questionnaire
     * answers, and their own change-request history. Resolved from the JWT login email, never a
     * caller-supplied id, so a vendor can only ever see their own data.
     */
    @GetMapping("/api/supplier-registration/my-profile")
    public ResponseEntity<ServiceResponse> getMyProfile() {
        String email = currentUserEmail();
        if (email == null) return ResponseEntity.status(401).build();
        ServiceResponse response = service.getMyProfile(email);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.status(404).body(response);
        return ResponseEntity.ok(response);
    }

    /**
     * A vendor requesting a change to one already-approved document, attachment, or questionnaire
     * answer. itemType is "document" (itemKey = docType e.g. "gst"), "attachment" (itemKey =
     * attachment id) or "answer" (itemKey = questionId). file is required for document/attachment;
     * newAnswerJson (same {textValue?/optionIds?/rows?} shape used by the rest of the form) is
     * required for answer. Goes into the "Vendor Change Request" workflow for the same approval
     * team as the original application — nothing here is applied until that's approved.
     */
    @PostMapping("/api/supplier-registration/change-requests")
    public ResponseEntity<ServiceResponse> submitChangeRequest(
            @RequestParam("itemType") String itemType,
            @RequestParam("itemKey") String itemKey,
            @RequestParam("reason") String reason,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "newAnswerJson", required = false) String newAnswerJson) {
        String email = currentUserEmail();
        if (email == null) return ResponseEntity.status(401).build();
        ServiceResponse response = vendorChangeRequestService.submit(email, itemType, itemKey, reason, file, newAnswerJson);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    /** Admin/employee reviewer detail for one change request — old value, reason, proposed new value. */
    @GetMapping("/api/supplier-registration/change-request/{changeRequestId}")
    public ResponseEntity<ServiceResponse> getChangeRequestForReview(@PathVariable Long changeRequestId) {
        ServiceResponse response = vendorChangeRequestService.getForReview(changeRequestId);
        if (AppConstants.ERRORCODE.equals(response.getErrorCode())) return ResponseEntity.status(404).body(response);
        return ResponseEntity.ok(response);
    }

    /** Same inline-stream preview pattern as previewDocument/previewAttachment, for the proposed new file. */
    @GetMapping("/api/supplier-registration/change-request/{changeRequestId}/preview")
    public ResponseEntity<byte[]> previewChangeRequestFile(@PathVariable Long changeRequestId) {
        try {
            com.example.multimedia.file_upload_api.service.FolderItService.DownloadedFile file =
                    vendorChangeRequestService.getProposedFilePreview(changeRequestId);
            return ResponseEntity.ok()
                    .header("Content-Type", file.contentType())
                    .header("Content-Disposition", "inline")
                    .header("Cache-Control", "no-store")
                    .body(file.bytes());
        } catch (Exception e) {
            logger.warn("Could not fetch preview for change request {}", changeRequestId, e);
            return ResponseEntity.status(404).build();
        }
    }

    /**
     * WorkFlow's fire_webhook() (webhook_utils.py) calls this on request.approved /
     * request.rejected for the "Vendor Approval" workflow. Verifies the
     * X-Signature-256 header the same way WorkFlow signs it (HMAC-SHA256 over the
     * raw body with a shared secret) before trusting the payload.
     */
    @PostMapping("/api/webhooks/vendor-approval")
    public ResponseEntity<String> vendorApprovalWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Signature-256", required = false) String signatureHeader) {
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            if (signatureHeader == null || !constantTimeEquals(signatureHeader, "sha256=" + hmacSha256(webhookSecret, rawBody))) {
                logger.warn("Rejected vendor-approval webhook — signature mismatch");
                return ResponseEntity.status(401).body("invalid signature");
            }
        }
        try {
            service.handleApprovalWebhook(new JSONObject(rawBody));
        } catch (Exception e) {
            logger.error("Failed to handle vendor-approval webhook", e);
            return ResponseEntity.status(500).body("error");
        }
        return ResponseEntity.ok("ok");
    }

    private static String hmacSha256(String secret, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
