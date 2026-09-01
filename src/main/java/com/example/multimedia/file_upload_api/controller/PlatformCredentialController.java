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
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Admin-facing view/edit of the credentials FolderItService and MicrovistaService use — see
 * PlatformCredentialService for why these moved out of hardcoded constants/env vars. Every key
 * here is one of a fixed, known set (not a generic arbitrary-key settings API), matching what
 * the admin portal's FolderIT Integration page actually renders as editable boxes.
 *
 * The fallback each key seeds with on first read MUST match the real default the owning
 * service used to hardcode/inject — an empty-string fallback here would silently seed (and
 * thereafter serve) a blank credential the moment anyone opens this page before the owning
 * service has ever made a real call, since PlatformCredentialService.get() persists whatever
 * fallback the first caller passes it. Caught this exact bug locally before it ever reached
 * production — never repeat "" as a fallback for a real secret here.
 */
@RestController
@RequestMapping("/api/admin/platform-credentials")
public class PlatformCredentialController {

    // Same literal defaults FolderItService hardcodes — duplicated here only so this page's
    // first read seeds correctly; the actual secret already lives in that file, this adds no
    // new exposure.
    private static final String DEFAULT_FOLDERIT_CLIENT_ID = "fPKfRJTOEyxFrNNH";
    private static final String DEFAULT_FOLDERIT_CLIENT_SECRET = "I8b5VmhC4Xdtfn-iqksE9r~HPu";
    private static final String DEFAULT_FOLDERIT_ACCOUNT_UID = "mprUk0ZilV";

    // Same env vars MicrovistaService reads, so this page's fallback always matches whatever
    // is actually configured in this process's environment.
    @Value("${microvista.token.id:}")
    private String defaultMicrovistaTokenId;

    @Value("${microvista.token.secret:}")
    private String defaultMicrovistaTokenSecret;

    private Map<String, String[]> keyLabels() {
        Map<String, String[]> m = new LinkedHashMap<>();
        m.put("folderit.client_id", new String[]{"folderit", "Client ID"});
        m.put("folderit.client_secret", new String[]{"folderit", "Client secret"});
        m.put("folderit.account_uid", new String[]{"folderit", "Account UID"});
        m.put("microvista.token_id", new String[]{"microvista", "Token ID"});
        m.put("microvista.token_secret", new String[]{"microvista", "Token secret"});
        // Consumed by MicrosoftAuthService — no prior hardcoded default to seed from, unlike
        // FolderIt/Microvista, since this integration never existed before.
        m.put("azure.tenant_id", new String[]{"azure", "Tenant ID"});
        m.put("azure.client_id", new String[]{"azure", "Client ID"});
        m.put("azure.client_secret", new String[]{"azure", "Client secret"});
        // Consumed by GoogleAuthService — same reasoning as azure.* above.
        m.put("google.client_id", new String[]{"google", "Client ID"});
        m.put("google.client_secret", new String[]{"google", "Client secret"});
        m.put("google.hosted_domain", new String[]{"google", "Workspace domain (optional)"});
        return m;
    }

    private String fallbackFor(String key) {
        return switch (key) {
            case "folderit.client_id" -> DEFAULT_FOLDERIT_CLIENT_ID;
            case "folderit.client_secret" -> DEFAULT_FOLDERIT_CLIENT_SECRET;
            case "folderit.account_uid" -> DEFAULT_FOLDERIT_ACCOUNT_UID;
            case "microvista.token_id" -> defaultMicrovistaTokenId;
            case "microvista.token_secret" -> defaultMicrovistaTokenSecret;
            default -> "";
        };
    }

    private final PlatformCredentialService credentials;
    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final AuthorizationRepository authorizationRepository;

    public PlatformCredentialController(PlatformCredentialService credentials,
                                         SuperAdminRepository superAdminRepository,
                                         UserDetailRepository userDetailRepository,
                                         UserAuthenticationRepository userAuthenticationRepository,
                                         AuthorizationRepository authorizationRepository) {
        this.credentials = credentials;
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.authorizationRepository = authorizationRepository;
    }

    @GetMapping
    public ResponseEntity<?> get() {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));

        Map<String, Object> groups = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : keyLabels().entrySet()) {
            String key = entry.getKey();
            String group = entry.getValue()[0];
            String label = entry.getValue()[1];
            String field = key.substring(key.indexOf('.') + 1);

            @SuppressWarnings("unchecked")
            Map<String, Object> groupMap = (Map<String, Object>) groups.computeIfAbsent(group, g -> new LinkedHashMap<String, Object>());
            Map<String, String> fieldOut = new LinkedHashMap<>();
            fieldOut.put("label", label);
            fieldOut.put("value", credentials.get(key, fallbackFor(key)));
            groupMap.put(field, fieldOut);
        }
        return ResponseEntity.ok(groups);
    }

    @PatchMapping
    public ResponseEntity<?> update(@RequestBody Map<String, String> payload) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("detail", "Admin access required for this account."));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            if (!keyLabels().containsKey(entry.getKey())) {
                return ResponseEntity.badRequest().body(Map.of("detail", "Unknown credential key: " + entry.getKey()));
            }
        }
        payload.forEach((key, value) -> credentials.set(key, value, email));
        return ResponseEntity.ok(Map.of("saved", payload.keySet()));
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

        String role = authorization.get().getAuthName().toUpperCase();
        return !role.equals("VENDOR") && !role.equals("EMPLOYEE") && !role.equals("PURCHASE_DEPT");
    }
}
