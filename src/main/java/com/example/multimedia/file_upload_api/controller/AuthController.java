package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.LoginRequest;
import com.example.multimedia.file_upload_api.dto.LoginResponse;
import com.example.multimedia.file_upload_api.dto.VendorPermissionResponseDto;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.security.JwtUtil;
import com.example.multimedia.file_upload_api.security.CustomUserDetailsService;
import com.example.multimedia.file_upload_api.service.VendorPermissionService;
import com.example.multimedia.file_upload_api.service.RolePermissionService;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.dto.PermissionItemDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private VendorPermissionService vendorPermissionService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.SuperAdminRepository superAdminRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);

            LoginResponse response = buildLoginResponse(userDetails, jwt);

            if (loginRequest.getLoginType() != null) {
                switch (loginRequest.getLoginType().toLowerCase()) {
                    case "vendor":
                        response.setRedirectUrl("/vendor/dashboard");
                        break;
                    case "employee":
                        response.setRedirectUrl("/employee/dashboard");
                        break;
                    case "super-admin":
                    case "standard":
                        response.setRedirectUrl("/admin/dashboard");
                        break;
                    default:
                        response.setRedirectUrl("/dashboard");
                        break;
                }
            } else {
                response.setRedirectUrl("/dashboard");
            }

            return ResponseEntity.ok(response);
        } catch (org.springframework.security.authentication.DisabledException e) {
            // CustomUserDetailsService now actually wires UserDetail.isActive/SuperAdmin.isActive
            // into isEnabled() — previously that flag had no real effect, so this branch could
            // never be reached; AuthenticationManager throws this before BadCredentialsException
            // would ever apply, so it needs its own explicit response rather than falling through
            // to "Invalid email or password" (misleading — the password may be entirely correct).
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "This account has been deactivated.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (BadCredentialsException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Turns an already-authenticated principal into the same rich payload /login returns — the
     * password path above passes in the UserDetails the AuthenticationManager just verified;
     * /session (below) passes in whatever JwtRequestFilter already put in the SecurityContext for
     * a valid bearer token; MicrosoftSsoController never calls this directly, it lands on /session
     * after issuing its own token, same as any other caller who already has a valid JWT would.
     * Does not set redirectUrl — that's login()'s own loginType-driven concern, callers that don't
     * have a loginType (i.e. /session) leave it for the frontend to compute from role, same as
     * AuthContext.jsx already does independently of whatever the server sends.
     */
    private LoginResponse buildLoginResponse(UserDetails userDetails, String jwt) {
        UserDetail user = userDetailRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        LoginResponse response;

        if (user != null) {
            UserAuthentication userAuth = userAuthenticationRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("User authentication not found"));

            response = new LoginResponse(
                jwt,
                user.getEmail(),
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber()
            );

            int authId = Integer.parseInt(userAuth.getAuthKey());
            Authorization auth = authorizationRepository.findById(authId)
                    .orElseThrow(() -> new RuntimeException("Authorization not found for ID: " + authId));

            response.setAuthId(auth.getAuthId());
            response.setAuthName(auth.getAuthName());
            response.setIsDocumentsPresent(checkDocumentsPresent(user.getCompany(), user.getEmail()));

            // Fetch permissions based on role type
            if (user.getUserType() == UserType.VENDOR || user.getUserType() == UserType.SUPER_ADMIN || user.getUserType() == null) {
                Long permissionLinkId = (user.getCompany() != null) ? user.getCompany().getCompanyId() : userAuth.getUserAuthenticationId();
                try {
                    VendorPermissionResponseDto permissions = vendorPermissionService.getPermissionsForLogin(permissionLinkId);
                    response.setPermissions(permissions);
                } catch (Exception e) {
                    // If no company exists with that ID, permissions will remain null
                }
            } else {
                try {
                    List<PermissionItemDto> permissions = rolePermissionService.getRolePermissionsTree(user.getUserType());
                    response.setPermissions(permissions);
                } catch (Exception e) {
                    // If errors occur, permissions will remain null
                }
            }
        } else {
            // It must be a SuperAdmin from the super_admin table. Downstream
            // callers (e.g. vendor_portal's Workflow tab) send this userId
            // straight through to WorkFlow, which only recognizes
            // user_details.user_id — so resolve a real shadow user_details
            // row for this admin instead of a dummy ID that matches nothing.
            Long shadowUserId = resolveSuperAdminShadowUserId(userDetails.getUsername());
            response = new LoginResponse(
                jwt,
                userDetails.getUsername(),
                shadowUserId,
                "Super",
                "Admin",
                ""
            );
            // Was hardcoded to 1 — resolve the real row instead, so this stays correct even if
            // the seeded super_admin Authorization row's id ever changes.
            int superAdminAuthId = authorizationRepository.findByAuthKeyIgnoreCase("super_admin")
                    .map(Authorization::getAuthId)
                    .orElse(1);
            response.setAuthId(superAdminAuthId);
            response.setAuthName("SUPER_ADMIN");
            response.setIsDocumentsPresent(true);
        }

        return response;
    }

    /**
     * Turns "I have a valid bearer token" into the same rich login payload /login returns —
     * lets any flow that ends up with a valid JWT but no LoginResponse yet (right now: the
     * Microsoft SSO callback, after it issues a token for an already-existing account) fetch one
     * without re-implementing the permissions/authId/shadow-superadmin logic above.
     */
    @GetMapping("/session")
    public ResponseEntity<?> session() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not signed in."));
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);
        LoginResponse response = buildLoginResponse(userDetails, jwt);
        response.setRedirectUrl("/dashboard");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate-hash/{password}")
    public String generateHash(@PathVariable String password) {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password);
    }

    /**
     * Super admins live only in the super_admin table, but every cross-system
     * caller that needs a "real" user id (e.g. WorkFlow, which only recognizes
     * user_details.user_id) can't resolve one for them. Finds or creates an
     * idempotent shadow user_details row per super admin, keyed by a
     * deterministic internal email so it never collides with — or gets
     * mistaken for — the admin's real login row. No UserAuthentication row is
     * created for it, so it can never itself be used to log in (same pattern
     * as the supplier-intake service account).
     */
    private Long resolveSuperAdminShadowUserId(String superAdminEmail) {
        SuperAdmin superAdmin = superAdminRepository.findByEmail(superAdminEmail)
                .orElseThrow(() -> new RuntimeException("Super admin not found: " + superAdminEmail));

        String shadowEmail = "superadmin-" + superAdmin.getSuperAdminId() + "@internal";
        return userDetailRepository.findByEmail(shadowEmail)
                .map(UserDetail::getUserId)
                .orElseGet(() -> {
                    UserDetail shadow = new UserDetail();
                    shadow.setSuperAdmin(superAdmin);
                    shadow.setEmail(shadowEmail);
                    shadow.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    shadow.setFirstName(superAdmin.getFirstName());
                    shadow.setLastName(superAdmin.getLastName());
                    shadow.setUserType(UserType.SUPER_ADMIN);
                    shadow.setIsActive(true);
                    return userDetailRepository.save(shadow).getUserId();
                });
    }

    private boolean checkDocumentsPresent(CompanyDetails company, String email) {
        if (company == null) {
            return false;
        }
        boolean isGstPresent = company.getGstinNumber() != null && !company.getGstinNumber().trim().isEmpty();
        boolean isPanPresent = company.getPanDetails() != null;
        boolean isChequePresent = company.getChequeDetails() != null;
        boolean isCoiPresent = company.getCertificateOfIncorporation() != null;
        boolean isMsmePresent = company.getMsmeDetails() != null;
        boolean isItrPresent = company.getItrDetails() != null;

        return isGstPresent && isPanPresent && isChequePresent && isCoiPresent && isMsmePresent && isItrPresent;
    }

}
