package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.SupplierDraftDTO;
import com.example.multimedia.file_upload_api.service.QuestionnaireService;
import com.example.multimedia.file_upload_api.service.SupplierRegistrationService;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    @Value("${workflow.webhook.secret:}")
    private String webhookSecret;

    public SupplierRegistrationController(SupplierRegistrationService service, QuestionnaireService questionnaireService) {
        this.service = service;
        this.questionnaireService = questionnaireService;
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
