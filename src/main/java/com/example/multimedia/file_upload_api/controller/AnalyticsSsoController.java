package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.PlatformCredentialService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

/**
 * Single sign-on into the NexD Report Designer (a separate deployed app under /analytics/):
 * an already-authenticated admin never sees NexD's own login form. This endpoint holds the
 * one real NexD admin credential server-side — via PlatformCredentialService, same as
 * FolderIt/Microvista — and exchanges it for a short-lived NexD JWT on the caller's behalf,
 * so the raw credential itself is never sent to the browser, only the resulting token.
 */
@RestController
public class AnalyticsSsoController {

    // The real NexD admin credentials, seeded once per environment directly into
    // platform_credential (local vs production each have their own NexD admin account/password)
    // — these fallbacks only matter if that seeding is ever lost, and only match production.
    private static final String DEFAULT_ANALYTICS_USERNAME = "admin";
    private static final String DEFAULT_ANALYTICS_PASSWORD = "LMYq1ConYwOMAhwbtl7G6g";

    @Value("${analytics.base-url:http://127.0.0.1:5090}")
    private String analyticsBaseUrl;

    private final PlatformCredentialService credentials;
    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final AuthorizationRepository authorizationRepository;
    private final RestTemplate restTemplate;

    public AnalyticsSsoController(PlatformCredentialService credentials,
                                   SuperAdminRepository superAdminRepository,
                                   UserDetailRepository userDetailRepository,
                                   UserAuthenticationRepository userAuthenticationRepository,
                                   AuthorizationRepository authorizationRepository,
                                   RestTemplate restTemplate) {
        this.credentials = credentials;
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.authorizationRepository = authorizationRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/api/admin/analytics-sso-token")
    public ResponseEntity<?> getSsoToken() {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("detail", "Admin access required."));

        String username = credentials.get("analytics.username", DEFAULT_ANALYTICS_USERNAME);
        String password = credentials.get("analytics.password", DEFAULT_ANALYTICS_PASSWORD);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.postForObject(
                    analyticsBaseUrl + "/api/auth/login",
                    Map.of("username", username, "password", password),
                    Map.class);
            return ResponseEntity.ok(Map.of("token", resp.get("token")));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("detail", "Could not reach the analytics service."));
        }
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        String email = auth.getName();

        if (superAdminRepository.existsByEmail(email)) return true;

        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        Optional<UserAuthentication> authOpt = userAuthenticationRepository.findByUserId(userOpt.get().getUserId());
        if (authOpt.isEmpty()) return false;

        String authKey = authOpt.get().getAuthKey();
        Optional<Authorization> authorization;
        try {
            authorization = authorizationRepository.findById(Integer.parseInt(authKey));
        } catch (NumberFormatException e) {
            authorization = authorizationRepository.findByAuthKeyIgnoreCase(authKey);
        }
        if (authorization.isEmpty()) return false;

        String roleName = authorization.get().getAuthName().toUpperCase();
        return !roleName.equals("VENDOR") && !roleName.equals("EMPLOYEE") && !roleName.equals("PURCHASE_DEPT");
    }
}
