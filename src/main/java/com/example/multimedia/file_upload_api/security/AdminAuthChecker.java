package com.example.multimedia.file_upload_api.security;

import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Shared "is the current caller an admin" check — was copy-pasted as a private method into
 * SupplierRegistrationController, QuestionnaireProxyController, PlatformCredentialController,
 * AnalyticsSsoController and EmailController; pulled out here rather than adding a 6th, 7th,
 * 8th and 9th copy for the new company/plant/purchasing-org/purchasing-group controllers. The
 * existing five callers are left as-is (not a fault, just not worth churning working code) —
 * new admin-only endpoints should use this instead.
 */
@Component
public class AdminAuthChecker {

    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final AuthorizationRepository authorizationRepository;

    public AdminAuthChecker(SuperAdminRepository superAdminRepository,
                             UserDetailRepository userDetailRepository,
                             UserAuthenticationRepository userAuthenticationRepository,
                             AuthorizationRepository authorizationRepository) {
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.authorizationRepository = authorizationRepository;
    }

    public boolean isAdmin() {
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
