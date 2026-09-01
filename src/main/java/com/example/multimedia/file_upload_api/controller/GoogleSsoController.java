package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.security.CustomUserDetailsService;
import com.example.multimedia.file_upload_api.security.JwtUtil;
import com.example.multimedia.file_upload_api.service.GoogleAuthService;
import com.example.multimedia.file_upload_api.service.LoginAttemptService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Google sign-in for staff — same shape as MicrosoftSsoController: "sign in as an account that
 * already exists," not "create an account for anyone with a Google login." Both endpoints here
 * are browser redirects, not JSON APIs the SPA fetches — failures redirect (to the login page
 * with an error reason) rather than returning a JSON error nobody would see.
 */
@RestController
public class GoogleSsoController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSsoController.class);
    private static final String CSRF_COOKIE = "gg_sso_state";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GoogleAuthService googleAuthService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final String publicBaseUrl;

    public GoogleSsoController(GoogleAuthService googleAuthService,
                                CustomUserDetailsService userDetailsService,
                                JwtUtil jwtUtil,
                                LoginAttemptService loginAttemptService,
                                Environment env) {
        this.googleAuthService = googleAuthService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
        this.publicBaseUrl = env.getProperty("app.public-base-url", "http://localhost:5173");
    }

    @GetMapping("/api/auth/google/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        try {
            String csrfBinder = randomBinder();
            setCsrfCookie(response, csrfBinder, 600);
            response.sendRedirect(googleAuthService.buildAuthorizeUrl(csrfBinder));
        } catch (IllegalStateException e) {
            response.sendRedirect(loginUrl("not_configured"));
        }
    }

    @GetMapping("/api/auth/google/callback")
    public void callback(@RequestParam(required = false) String code,
                          @RequestParam(required = false) String state,
                          @RequestParam(required = false) String error,
                          HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        // Single-use regardless of outcome — clear it before doing anything else so a failed
        // attempt can't be retried with the same cookie.
        String csrfBinder = readCsrfCookie(request);
        clearCsrfCookie(response);

        if (error != null || code == null) {
            loginAttemptService.record("unknown", "GOOGLE", false, "cancelled");
            response.sendRedirect(loginUrl("cancelled"));
            return;
        }

        String email;
        try {
            email = googleAuthService.exchangeCodeForEmail(code, state, csrfBinder);
        } catch (Exception e) {
            logger.warn("Google sign-in exchange failed: {}", e.getMessage());
            loginAttemptService.record("unknown", "GOOGLE", false, "exchange_failed");
            response.sendRedirect(loginUrl("exchange_failed"));
            return;
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            loginAttemptService.record(email, "GOOGLE", false, "no_account");
            response.sendRedirect(loginUrl("no_account"));
            return;
        }

        // Password login gets this check for free from AuthenticationManager/
        // DaoAuthenticationProvider; this path never goes through that (there's no password to
        // check), so it has to ask explicitly. Covers both isActive=false UserDetail rows and a
        // deactivated SuperAdmin — CustomUserDetailsService wires both into isEnabled() now.
        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()
                || !userDetails.isAccountNonExpired() || !userDetails.isCredentialsNonExpired()) {
            loginAttemptService.record(email, "GOOGLE", false, "account_disabled");
            response.sendRedirect(loginUrl("account_disabled"));
            return;
        }

        loginAttemptService.record(email, "GOOGLE", true, null);
        String jwt = jwtUtil.generateToken(userDetails);
        response.sendRedirect(publicBaseUrl + "/auth/callback?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8));
    }

    private String loginUrl(String reason) {
        return publicBaseUrl + "/login?sso_error=" + reason;
    }

    private static String randomBinder() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void setCsrfCookie(HttpServletResponse response, String value, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(CSRF_COOKIE, value)
                .httpOnly(true)
                .secure(publicBaseUrl.startsWith("https://"))
                .sameSite("Lax")
                .path("/api/auth/google")
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCsrfCookie(HttpServletResponse response) {
        setCsrfCookie(response, "", 0);
    }

    private String readCsrfCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (CSRF_COOKIE.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
