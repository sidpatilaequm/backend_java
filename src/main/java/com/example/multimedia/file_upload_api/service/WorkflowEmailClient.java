package com.example.multimedia.file_upload_api.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Sends one of WorkFlow's admin-editable templates (email_templates table) by mail_key, instead
 * of building an email inline in Java. Shared by every caller that needs a transactional email —
 * pulled out of SupplierRegistrationService (which had its own private copy of this same HTTP
 * call) so a second caller (PurchaseRequisitionServiceImpl's RFQ-assignment email) doesn't have
 * to duplicate it a third time.
 */
@Service
public class WorkflowEmailClient {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEmailClient.class);

    private final RestTemplate restTemplate;

    @Value("${workflow.api.base-url:http://localhost:8000}")
    private String workflowBaseUrl;

    @Value("${workflow.email-templates.service-token:}")
    private String workflowServiceToken;

    public WorkflowEmailClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void trigger(String mailKey, String toEmail, Map<String, Object> variables) {
        trigger(mailKey, toEmail, variables, null);
    }

    /**
     * @param toneOverride optional status-strip tone ("info"/"ok"/"warn"/"bad") for a template
     *                      shared across multiple outcomes (e.g. VCR.2's approve vs. reject) —
     *                      null keeps the template's own fixed tone.
     */
    public void trigger(String mailKey, String toEmail, Map<String, Object> variables, String toneOverride) {
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
            if (toneOverride != null) {
                payload.put("tone_override", toneOverride);
            }
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
}
