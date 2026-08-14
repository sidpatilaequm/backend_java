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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final FolderItService folderItService;
    private final OpenAiVisionOcrService ocrService;
    private final MicrovistaService microvistaService;
    private final UserDetailRepository userDetailRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final CompanyDetailsRepository companyDetailsRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AuthorizationRepository authorizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;
    private final ServiceControllerUtils serviceControllerUtils;

    @Value("${workflow.api.base-url:http://localhost:8000}")
    private String workflowBaseUrl;

    @Value("${workflow.vendor-approval.id:8}")
    private Long vendorApprovalWorkflowId;

    @Value("${mail.from:}")
    private String mailFrom;

    public SupplierRegistrationService(SupplierRegistrationRepository registrationRepository,
                                        SupplierRegistrationDocumentRepository documentRepository,
                                        FolderItService folderItService,
                                        OpenAiVisionOcrService ocrService,
                                        MicrovistaService microvistaService,
                                        UserDetailRepository userDetailRepository,
                                        UserAuthenticationRepository userAuthenticationRepository,
                                        CompanyDetailsRepository companyDetailsRepository,
                                        SuperAdminRepository superAdminRepository,
                                        AuthorizationRepository authorizationRepository,
                                        PasswordEncoder passwordEncoder,
                                        JavaMailSender mailSender,
                                        RestTemplate restTemplate,
                                        ServiceControllerUtils serviceControllerUtils) {
        this.registrationRepository = registrationRepository;
        this.documentRepository = documentRepository;
        this.folderItService = folderItService;
        this.ocrService = ocrService;
        this.microvistaService = microvistaService;
        this.userDetailRepository = userDetailRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.companyDetailsRepository = companyDetailsRepository;
        this.superAdminRepository = superAdminRepository;
        this.authorizationRepository = authorizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.restTemplate = restTemplate;
        this.serviceControllerUtils = serviceControllerUtils;
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

            OpenAiVisionOcrService.ExtractResult extracted = ocrService.extractFields(docType, file);
            String folderItUid = folderItService.uploadFileToFolderIt(file, "vendor", registration.getVendorName(), null);

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

            reg.setVendorName(dto.getVendorName());
            reg.setAddress(dto.getAddress());
            reg.setContactName(dto.getContactName());
            reg.setDesignation(dto.getDesignation());
            if (dto.getEmail() != null && !dto.getEmail().isBlank()) reg.setEmail(dto.getEmail());
            reg.setPhone(dto.getPhone());
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

            if (reg.getResumeCode() == null) {
                reg.setResumeCode(generateResumeCode());
            }
            reg = registrationRepository.save(reg);

            sendResumeCodeEmail(reg);

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

    private String generateResumeCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (registrationRepository.findByResumeCode(code).isPresent());
        return code;
    }

    private void sendResumeCodeEmail(SupplierRegistration reg) {
        if (mailFrom == null || mailFrom.isBlank() || reg.getEmail() == null || reg.getEmail().contains("@placeholder.local")) {
            logger.warn("Skipping resume-code email — mail not configured or no real email yet");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(reg.getEmail());
            message.setSubject("Your supplier registration draft code");
            message.setText("Your draft has been saved.\n\nResume code: " + reg.getResumeCode()
                    + "\n\nEnter this code on the Become a Supplier page to pick up where you left off.");
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send resume-code email to {}", reg.getEmail(), e);
        }
    }

    public ServiceResponse getDraftByCode(String code) {
        ServiceResponse response = new ServiceResponse();
        SupplierRegistration reg = registrationRepository.findByResumeCode(code).orElse(null);
        if (reg == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "No draft found for that code");
        }
        List<SupplierRegistrationDocument> docs = documentRepository.findByRegistrationId(reg.getId());
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
        response.addData("result", data);
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Draft loaded");
    }

    // ── Submit into WorkFlow's "Vendor Approval" workflow ────────────────

    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse submit(Long registrationId) {
        ServiceResponse response = new ServiceResponse();
        try {
            SupplierRegistration reg = registrationRepository.findById(registrationId)
                    .orElseThrow(() -> new RuntimeException("Registration not found"));

            List<String> missing = new ArrayList<>();
            for (SupplierDocumentConfig.DocDef d : SupplierDocumentConfig.DOCS) {
                if (!d.required()) continue;
                boolean present = documentRepository.findByRegistrationIdAndDocType(reg.getId(), d.id())
                        .map(doc -> doc.getFolderItFileUid() != null).orElse(false);
                if (!present) missing.add(d.name());
            }
            if (reg.getEmail() == null || reg.getEmail().contains("@placeholder.local")) missing.add(0, "a real contact email");
            if (!missing.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE,
                        "Missing required: " + String.join(", ", missing));
            }

            UserDetail intakeUser = userDetailRepository.findByEmail(SupplierIntakeSeeder.INTAKE_EMAIL)
                    .orElseThrow(() -> new RuntimeException("Intake service account not found"));

            JSONObject metadata = new JSONObject()
                    .put("registrationId", reg.getId())
                    .put("vendorName", reg.getVendorName())
                    .put("contactName", reg.getContactName())
                    .put("email", reg.getEmail())
                    .put("phone", reg.getPhone())
                    .put("gstNumber", reg.getGstNumber())
                    .put("panNumber", reg.getPanNumber());

            JSONObject payload = new JSONObject()
                    .put("title", "Supplier registration — " + reg.getVendorName())
                    .put("description", "Self-serve Become-a-Supplier submission")
                    .put("request_type", "vendor_registration")
                    .put("department", JSONObject.NULL)
                    .put("workflow_id", vendorApprovalWorkflowId)
                    .put("request_metadata", metadata);

            String url = UriComponentsBuilder.fromHttpUrl(workflowBaseUrl + "/api/workflows/requests/")
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

        SupplierRegistration reg = registrationRepository.findByWorkflowRequestId(workflowRequestId).orElse(null);
        if (reg == null) {
            logger.warn("No supplier_registration found for workflow_request_id={}", workflowRequestId);
            return;
        }

        if ("request.approved".equals(event)) {
            provisionVendorAccount(reg);
        } else if ("request.rejected".equals(event)) {
            reg.setStatus("REJECTED");
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

        reg.setStatus("ACTIVE");
        reg.setVendorCode(vendorCode);
        reg.setUserId(user.getUserId());
        reg.setCompanyId(company.getCompanyId());
        reg.setApprovedDate(LocalDateTime.now());
        registrationRepository.save(reg);

        sendCredentialsEmail(reg, rawPassword);
    }

    private void sendCredentialsEmail(SupplierRegistration reg, String rawPassword) {
        if (mailFrom == null || mailFrom.isBlank()) {
            logger.warn("Skipping credentials email — mail not configured");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(reg.getEmail());
            message.setSubject("You're approved — your vendor login");
            message.setText("Congratulations — your supplier registration has been approved.\n\n"
                    + "Vendor code: " + reg.getVendorCode()
                    + "\nLogin email: " + reg.getEmail()
                    + "\nPassword: " + rawPassword
                    + "\n\nPlease log in and change your password.");
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send credentials email to {}", reg.getEmail(), e);
        }
    }
}
