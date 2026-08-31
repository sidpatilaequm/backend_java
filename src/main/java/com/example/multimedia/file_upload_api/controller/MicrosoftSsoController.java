package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.security.CustomUserDetailsService;
import com.example.multimedia.file_upload_api.security.JwtUtil;
import com.example.multimedia.file_upload_api.service.MicrosoftAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Microsoft Entra ID sign-in for staff — "sign in as an account that already exists," not "create
 * an account for anyone with a Microsoft login." Both endpoints here are browser redirects, not
 * JSON APIs the SPA fetches: the whole point of the authorization-code flow is that the browser
 * itself bounces to Microsoft and back, so failures here also redirect (to the login page with an
 * error reason) rather than returning a JSON error nobody would see.
 */
@RestController
public class MicrosoftSsoController {

    private static final Logger logger = LoggerFactory.getLogger(MicrosoftSsoController.class);

    private final MicrosoftAuthService microsoftAuthService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final String publicBaseUrl;

    public MicrosoftSsoController(MicrosoftAuthService microsoftAuthService,
                                   CustomUserDetailsService userDetailsService,
                                   JwtUtil jwtUtil,
                                   Environment env) {
        this.microsoftAuthService = microsoftAuthService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.publicBaseUrl = env.getProperty("app.public-base-url", "http://localhost:5173");
    }

    @GetMapping("/api/auth/microsoft/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        try {
            response.sendRedirect(microsoftAuthService.buildAuthorizeUrl());
        } catch (IllegalStateException e) {
            response.sendRedirect(loginUrl("not_configured"));
        }
    }

    @GetMapping("/api/auth/microsoft/callback")
    public void callback(@RequestParam(required = false) String code,
                          @RequestParam(required = false) String state,
                          @RequestParam(required = false) String error,
                          HttpServletResponse response) throws IOException {
        if (error != null || code == null) {
            response.sendRedirect(loginUrl("cancelled"));
            return;
        }

        String email;
        try {
            email = microsoftAuthService.exchangeCodeForEmail(code, state);
        } catch (Exception e) {
            logger.warn("Microsoft sign-in exchange failed: {}", e.getMessage());
            response.sendRedirect(loginUrl("exchange_failed"));
            return;
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            response.sendRedirect(loginUrl("no_account"));
            return;
        }

        String jwt = jwtUtil.generateToken(userDetails);
        response.sendRedirect(publicBaseUrl + "/auth/callback?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8));
    }

    private String loginUrl(String reason) {
        return publicBaseUrl + "/login?sso_error=" + reason;
    }
}
