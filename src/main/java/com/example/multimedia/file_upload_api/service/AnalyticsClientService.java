package com.example.multimedia.file_upload_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Server-to-server client for the analytics (Report Designer) service — mirrors
 * AnalyticsSsoController's credential exchange (same PlatformCredentialService keys,
 * `analytics.username`/`analytics.password`, and the same `analytics.base-url` property) but
 * for backend_java's own calls, not a human admin's browser session. Used to list what's
 * currently published to a role (see EmployeeReportsController) without ever handing the
 * analytics JWT or the report's own share token to the browser.
 */
@Service
public class AnalyticsClientService {

    private static final String DEFAULT_ANALYTICS_USERNAME = "admin";
    private static final String DEFAULT_ANALYTICS_PASSWORD = "LMYq1ConYwOMAhwbtl7G6g";

    @Value("${analytics.base-url:http://127.0.0.1:5090}")
    private String analyticsBaseUrl;

    private final PlatformCredentialService credentials;
    private final RestTemplate restTemplate;

    // The analytics JWT is cheap to re-fetch but not free — cached in-process with a
    // conservative TTL well under whatever analytics itself expires it at, rather than calling
    // /api/auth/login on every single Dashboards tab load.
    private volatile String cachedToken;
    private volatile Instant cachedTokenExpiry = Instant.EPOCH;
    private static final long TOKEN_TTL_SECONDS = 10 * 60;

    public AnalyticsClientService(PlatformCredentialService credentials, RestTemplate restTemplate) {
        this.credentials = credentials;
        this.restTemplate = restTemplate;
    }

    public record PublishedReport(String key, String name, String updatedAt, String url) {}

    @SuppressWarnings("unchecked")
    public List<PublishedReport> listPublished(String role) {
        String token = getToken();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        var entity = new org.springframework.http.HttpEntity<>(headers);

        var response = restTemplate.exchange(
                analyticsBaseUrl + "/api/processes/published?role=" + role,
                org.springframework.http.HttpMethod.GET, entity, Map.class);

        Map<String, Object> body = response.getBody();
        List<Map<String, Object>> reports = body == null
                ? List.of() : (List<Map<String, Object>>) body.getOrDefault("reports", List.of());

        return reports.stream()
                .map(r -> new PublishedReport(
                        String.valueOf(r.get("key")),
                        String.valueOf(r.get("name")),
                        r.get("updatedAt") == null ? null : String.valueOf(r.get("updatedAt")),
                        String.valueOf(r.get("url"))))
                .toList();
    }

    private synchronized String getToken() {
        if (cachedToken != null && Instant.now().isBefore(cachedTokenExpiry)) {
            return cachedToken;
        }
        String username = credentials.get("analytics.username", DEFAULT_ANALYTICS_USERNAME);
        String password = credentials.get("analytics.password", DEFAULT_ANALYTICS_PASSWORD);

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(
                analyticsBaseUrl + "/api/auth/login",
                Map.of("username", username, "password", password),
                Map.class);
        if (resp == null || resp.get("token") == null) {
            throw new RuntimeException("Could not authenticate with the analytics service.");
        }
        cachedToken = String.valueOf(resp.get("token"));
        cachedTokenExpiry = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);
        return cachedToken;
    }
}
