package com.example.multimedia.file_upload_api.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Microsoft Entra ID (Azure AD) sign-in for staff, hand-rolled the same way FolderItService talks
 * to FolderIt's OAuth2 endpoint — plain OkHttp calls, not Spring Security's oauth2Login()/session
 * machinery, since this app is stateless (see SecurityConfig) and oauth2Login() assumes a session
 * across the authorize -> callback redirect. The one piece worth a real library rather than
 * hand-rolling is verifying the id_token Microsoft hands back (signature + issuer), which uses
 * Spring's NimbusJwtDecoder.
 *
 * "Sign-in only works for an existing account" is enforced by the caller (MicrosoftSsoController),
 * not here — this service's job stops at "here is the verified email Microsoft is vouching for."
 */
@Service
public class MicrosoftAuthService {

    private final PlatformCredentialService credentials;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final String publicBaseUrl;

    // Signs the short-lived `state` value only (CSRF protection for the redirect round trip) —
    // deliberately separate from JwtUtil's session-token signing key, since this protects a
    // different, much lower-stakes thing (a 10-minute request correlator, not a login session).
    private static final String STATE_SECRET =
            "8f2b6e1a9d4c7f30b5e8a1d6c9f2b5e8a1d6c9f2b5e8a1d6c9f2b5e8a1d6c9f2";
    private static final long STATE_TTL_MS = 10 * 60 * 1000;

    public MicrosoftAuthService(PlatformCredentialService credentials,
                                 org.springframework.core.env.Environment env) {
        this.credentials = credentials;
        this.publicBaseUrl = env.getProperty("app.public-base-url", "http://localhost:5173");
    }

    private String tenantId() { return credentials.get("azure.tenant_id", ""); }
    private String clientId() { return credentials.get("azure.client_id", ""); }
    private String clientSecret() { return credentials.get("azure.client_secret", ""); }

    private String redirectUri() {
        return publicBaseUrl + "/api/auth/microsoft/callback";
    }

    public String buildAuthorizeUrl() {
        String tenant = tenantId();
        String client = clientId();
        if (tenant.isBlank() || client.isBlank()) {
            throw new IllegalStateException(
                    "Microsoft sign-in isn't configured yet — add the tenant and client ID under "
                            + "Admin > System Settings > Directory (SSO) first.");
        }
        return "https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/authorize"
                + "?client_id=" + encode(client)
                + "&response_type=code"
                + "&redirect_uri=" + encode(redirectUri())
                + "&response_mode=query"
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(signState());
    }

    /** Exchanges the authorization code for tokens, verifies the id_token, returns the signed-in
     *  email. Throws IllegalStateException/JwtException on anything that doesn't check out —
     *  the caller turns that into a friendly redirect, not a stack trace shown to anyone. */
    public String exchangeCodeForEmail(String code, String state) throws IOException {
        verifyState(state);

        String tenant = tenantId();
        String client = clientId();

        RequestBody body = new FormBody.Builder()
                .add("client_id", client)
                .add("client_secret", clientSecret())
                .add("code", code)
                .add("redirect_uri", redirectUri())
                .add("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url("https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/token")
                .post(body)
                .build();

        String idToken;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Microsoft rejected the sign-in (" + response.code() + ").");
            }
            JSONObject json = new JSONObject(response.body().string());
            idToken = json.getString("id_token");
        }

        JwtDecoder decoder = NimbusJwtDecoder
                .withIssuerLocation("https://login.microsoftonline.com/" + tenant + "/v2.0")
                .build();
        Jwt jwt = decoder.decode(idToken); // throws if signature/issuer/expiry don't check out

        if (!jwt.getAudience().contains(client)) {
            throw new IllegalStateException("id_token audience did not match this app's client ID.");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) email = jwt.getClaimAsString("preferred_username");
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Microsoft did not return an email for this account.");
        }
        return email;
    }

    // -- state: a short-lived signed token, not a server-side session/cache entry — accepts a
    // narrow (<=10 min) replay window on the state value itself as the tradeoff for that; the
    // actual authorization code is still single-use, enforced by Microsoft. --

    private String signState() {
        return Jwts.builder()
                .setSubject("microsoft-sso-state")
                .setExpiration(new Date(System.currentTimeMillis() + STATE_TTL_MS))
                .signWith(stateSigningKey())
                .compact();
    }

    private void verifyState(String state) {
        if (state == null || state.isBlank()) {
            throw new IllegalStateException("Missing sign-in state.");
        }
        try {
            Jwts.parserBuilder().setSigningKey(stateSigningKey()).build().parseClaimsJws(state);
        } catch (Exception e) {
            throw new IllegalStateException("Sign-in link expired or was tampered with — try again.");
        }
    }

    private Key stateSigningKey() {
        return Keys.hmacShaKeyFor(STATE_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
