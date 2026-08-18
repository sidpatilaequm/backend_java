package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.QuestionnaireService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authenticated, admin-role-gated pass-through to Form Studio (dynamic_questions) for every
 * process/section/question CRUD, ordering, duplicate and activate operation. Form Studio itself
 * has no auth of its own (by design — see its README) and is bound to loopback in production, so
 * this controller is the only way the browser can reach it. Everything under here forwards the
 * same sub-path onto Form Studio's own /api/**, so e.g. a request to
 * /api/questionnaire/processes/3/activate becomes /api/processes/3/activate on Form Studio.
 *
 * The seller-facing Become-a-Supplier flow does NOT go through this controller — see
 * QuestionnaireService, which reads/writes the same shared-DB tables directly via JPA so
 * registration submission never depends on Form Studio's uptime.
 */
@RestController
public class QuestionnaireProxyController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireProxyController.class);

    @Value("${form.studio.base-url}")
    private String formStudioBaseUrl;

    // Not the shared RestTemplate bean — its default transport (JDK HttpURLConnection) has no
    // PATCH support at all, and Spring Boot 3's Apache-HttpClient-backed alternative requires
    // HttpClient 5.x, which isn't on this project's classpath (see RestTemplateConfig). The JDK's
    // own java.net.http.HttpClient (11+) supports every method correctly with zero dependencies.
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private static final Pattern DELETE_PROCESS_PATH = Pattern.compile("^/api/questionnaire/processes/(\\d+)/?$");

    private final UserDetailRepository userDetailRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final AuthorizationRepository authorizationRepository;
    private final SuperAdminRepository superAdminRepository;
    private final QuestionnaireService questionnaireService;

    public QuestionnaireProxyController(UserDetailRepository userDetailRepository,
                                         UserAuthenticationRepository userAuthenticationRepository,
                                         AuthorizationRepository authorizationRepository,
                                         SuperAdminRepository superAdminRepository,
                                         QuestionnaireService questionnaireService) {
        this.userDetailRepository = userDetailRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.authorizationRepository = authorizationRepository;
        this.superAdminRepository = superAdminRepository;
        this.questionnaireService = questionnaireService;
    }

    /**
     * How many applicants have a draft in progress against this questionnaire — Form Studio has
     * no visibility into this at all (a draft only becomes a real response at submit time), so
     * this is the one thing the admin builder can't get from the proxied Form Studio endpoints
     * and needs its own route for. A specific @GetMapping takes precedence over the wildcard
     * proxy below, so this never gets forwarded to Form Studio by mistake.
     */
    @GetMapping("/api/questionnaire/{processId}/draft-count")
    public ResponseEntity<String> draftCount(@PathVariable Integer processId) {
        if (!isAdmin()) {
            return ResponseEntity.status(403)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("{\"detail\":\"Admin access required for this account.\"}");
        }
        long count = questionnaireService.countDraftsForProcess(processId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body("{\"draftCount\":" + count + "}");
    }

    @RequestMapping("/api/questionnaire/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) {
        if (!isAdmin()) {
            // JSON body matching Form Studio's own {"detail": "..."} error shape, since the
            // frontend's error handling reads err.response.data.detail — a plain-text body here
            // would silently fall through to a generic "could not save" message instead.
            return ResponseEntity.status(403)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("{\"detail\":\"Admin access required for this account.\"}".getBytes());
        }

        String fullPath = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI();

        // Blocked here, before ever reaching Form Studio — its own delete lock only knows about
        // *submitted* responses, not in-progress drafts (which never become a Form Studio row
        // until submit). Deleting the questionnaire out from under someone mid-form would silently
        // orphan their answers.
        if ("DELETE".equals(request.getMethod())) {
            Matcher m = DELETE_PROCESS_PATH.matcher(fullPath);
            if (m.matches()) {
                long drafts = questionnaireService.countDraftsForProcess(Integer.parseInt(m.group(1)));
                if (drafts > 0) {
                    return ResponseEntity.status(409)
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .body(("{\"detail\":\"" + drafts + " applicant(s) have a draft in progress against this questionnaire, so it can't be deleted.\"}").getBytes());
                }
            }
        }

        String query = request.getQueryString();
        String subPath = fullPath.substring("/api/questionnaire".length());
        String url = formStudioBaseUrl + "/api" + subPath + (query != null ? "?" + query : "");

        String method = request.getMethod();
        byte[] body = readBody(request);
        String contentType = Optional.ofNullable(request.getContentType()).orElse(MediaType.APPLICATION_JSON_VALUE);

        try {
            HttpRequest upstreamRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", contentType)
                    .method(method, body.length > 0
                            ? HttpRequest.BodyPublishers.ofByteArray(body)
                            : HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<byte[]> upstream = httpClient.send(upstreamRequest, HttpResponse.BodyHandlers.ofByteArray());

            HttpHeaders outHeaders = new HttpHeaders();
            upstream.headers().firstValue("Content-Type").ifPresent(ct -> outHeaders.set(HttpHeaders.CONTENT_TYPE, ct));
            return ResponseEntity.status(upstream.statusCode()).headers(outHeaders).body(upstream.body());
        } catch (Exception e) {
            logger.error("Questionnaire proxy call failed: {} {}", method, url, e);
            return ResponseEntity.status(502)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(("{\"detail\":\"Questionnaire service unavailable: " + e.getMessage() + "\"}").getBytes());
        }
    }

    private byte[] readBody(HttpServletRequest request) {
        try {
            return request.getInputStream().readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * Mirrors vendor_portal's own RouteGuards logic (App.jsx, allowedRole="standard") exactly,
     * rather than checking UserDetail.userType — that enum column turns out to be a different,
     * inconsistently-populated field that the rest of the app doesn't actually gate admin-panel
     * access on. The real check the frontend already uses is looser: "not vendor, not
     * employee/purchase_dept".
     *
     * Two genuinely different account shapes both need to pass this:
     *  - Super admins (AuthController's login "else" branch): live ONLY in the super_admin
     *    table under their real email, which the JWT subject carries — but they also get an
     *    auto-created "shadow" user_details row under a synthetic superadmin-N@internal email
     *    for cross-system id references (WorkFlow etc). Looking up user_details by the JWT's
     *    real email finds nothing for these accounts, so they must be checked first, directly.
     *  - Everyone else: a real user_details row, whose UserAuthentication.authKey (the
     *    Authorization row's id, stored as a string) resolves to Authorization.authName — the
     *    same "role" value AuthContext.jsx derives from the login response's authName field.
     */
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
