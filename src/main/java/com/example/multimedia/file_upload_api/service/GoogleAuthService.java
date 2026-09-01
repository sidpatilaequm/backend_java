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
 * Google sign-in for staff — same shape and same non-negotiable as MicrosoftAuthService:
 * "sign in only for an account that already exists," enforced by the caller
 * (GoogleSsoController), not here. Hand-rolled OAuth2/OIDC for the same reason as Microsoft's —
 * this app is stateless (see SecurityConfig), which rules out Spring Security's oauth2Login().
 *
 * Kept fully independent from MicrosoftAuthService (own state-signing secret, own cookie, own
 * everything) rather than sharing code between the two providers — same way FolderIt and
 * Microvista are independent integrations rather than a shared "external service" abstraction.
 */
@Service
public class GoogleAuthService {

    private final PlatformCredentialService credentials;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final String publicBaseUrl;

    // Deliberately a different constant from MicrosoftAuthService.STATE_SECRET — a leaked key for
    // one provider's state signing must not let anyone forge state for the other.
    private static final String STATE_SECRET =
            "3d7c1f9a6b2e4508c1f6a9d3e7b0c4f8a1d5e9c2b6f0a4d8e1c5f9b3a7d0e4c8";
    private static final long STATE_TTL_MS = 10 * 60 * 1000;

    public GoogleAuthService(PlatformCredentialService credentials,
                              org.springframework.core.env.Environment env) {
        this.credentials = credentials;
        this.publicBaseUrl = env.getProperty("app.public-base-url", "http://localhost:5173");
    }

    private String clientId() { return credentials.get("google.client_id", ""); }
    private String clientSecret() { return credentials.get("google.client_secret", ""); }
    private String hostedDomain() { return credentials.get("google.hosted_domain", ""); }

    private String redirectUri() {
        return publicBaseUrl + "/api/auth/google/callback";
    }

    /**
     * csrfBinder ties this specific /authorize request to the specific browser that made it —
     * see MicrosoftAuthService.buildAuthorizeUrl's javadoc for the full login-CSRF rationale;
     * identical reasoning applies here.
     */
    public String buildAuthorizeUrl(String csrfBinder) {
        String client = clientId();
        if (client.isBlank()) {
            throw new IllegalStateException(
                    "Google sign-in isn't configured yet — add the client ID under "
                            + "Admin > System Settings > Directory (SSO) first.");
        }
        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + encode(client)
                + "&response_type=code"
                + "&redirect_uri=" + encode(redirectUri())
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(signState(csrfBinder));

        // A hint to Google's account chooser only — not itself enforcement. The real check is
        // the "hd" claim comparison in exchangeCodeForEmail below; someone could still pick a
        // different account at Google's prompt, so the server side can never skip that check.
        String hd = hostedDomain();
        if (!hd.isBlank()) {
            url += "&hd=" + encode(hd);
        }
        return url;
    }

    /** Exchanges the authorization code for tokens, verifies the id_token, returns the signed-in
     *  email. Throws IllegalStateException/JwtException on anything that doesn't check out —
     *  the caller turns that into a friendly redirect, not a stack trace shown to anyone.
     *  csrfBinder is the cookie value read back off the callback request — must match what's
     *  embedded in state. */
    public String exchangeCodeForEmail(String code, String state, String csrfBinder) throws IOException {
        verifyState(state, csrfBinder);

        String client = clientId();

        RequestBody body = new FormBody.Builder()
                .add("client_id", client)
                .add("client_secret", clientSecret())
                .add("code", code)
                .add("redirect_uri", redirectUri())
                .add("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(body)
                .build();

        String idToken;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Google rejected the sign-in (" + response.code() + ").");
            }
            JSONObject json = new JSONObject(response.body().string());
            idToken = json.getString("id_token");
        }

        JwtDecoder decoder = NimbusJwtDecoder
                .withIssuerLocation("https://accounts.google.com")
                .build();
        Jwt jwt = decoder.decode(idToken); // throws if signature/issuer/expiry don't check out

        if (!jwt.getAudience().contains(client)) {
            throw new IllegalStateException("id_token audience did not match this app's client ID.");
        }

        // Google can issue a valid, correctly-signed id_token for an unverified email (e.g. some
        // legacy/non-Gmail accounts) — Microsoft's id_token has no equivalent claim, which is why
        // this check has no counterpart on that path.
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        if (emailVerified != null && !emailVerified) {
            throw new IllegalStateException("Google reports this email address as unverified.");
        }

        String hd = hostedDomain();
        if (!hd.isBlank()) {
            String tokenHd = jwt.getClaimAsString("hd");
            if (tokenHd == null || !tokenHd.equalsIgnoreCase(hd)) {
                throw new IllegalStateException("This Google account is not on the expected Workspace domain.");
            }
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Google did not return an email for this account.");
        }
        return email;
    }

    // -- state: a short-lived signed token carrying the csrfBinder claim, same tradeoff as
    // MicrosoftAuthService's — see that class's comment for the full rationale. --

    private String signState(String csrfBinder) {
        return Jwts.builder()
                .setSubject("google-sso-state")
                .claim("binder", csrfBinder)
                .setExpiration(new Date(System.currentTimeMillis() + STATE_TTL_MS))
                .signWith(stateSigningKey())
                .compact();
    }

    private void verifyState(String state, String csrfBinder) {
        if (state == null || state.isBlank()) {
            throw new IllegalStateException("Missing sign-in state.");
        }
        if (csrfBinder == null || csrfBinder.isBlank()) {
            throw new IllegalStateException("Missing or expired sign-in cookie — try again.");
        }
        String embeddedBinder;
        try {
            embeddedBinder = Jwts.parserBuilder().setSigningKey(stateSigningKey()).build()
                    .parseClaimsJws(state).getBody().get("binder", String.class);
        } catch (Exception e) {
            throw new IllegalStateException("Sign-in link expired or was tampered with — try again.");
        }
        if (!csrfBinder.equals(embeddedBinder)) {
            throw new IllegalStateException("Sign-in did not originate from this browser — try again.");
        }
    }

    private Key stateSigningKey() {
        return Keys.hmacShaKeyFor(STATE_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
