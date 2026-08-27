package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.EmailRequestDTO;
import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SuperAdminRepository superAdminRepository;
    @Autowired
    private UserDetailRepository userDetailRepository;
    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;
    @Autowired
    private AuthorizationRepository authorizationRepository;

    // Raw arbitrary-subject/body send to any userId — was reachable by any authenticated
    // account (vendor, employee, ...), not just admins, since it only sat behind the app's
    // default "authenticated()" rule. Admin-gated the same way every other admin-only
    // controller in this app is (see PlatformCredentialController/QuestionnaireProxyController).
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDTO request) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("Admin access required for this account.");
        }
        try {
            emailService.sendSimpleEmailToUserId(request.getUserId(), request.getSubject(), request.getBody());
            return ResponseEntity.ok("Email sent successfully to user ID " + request.getUserId());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
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

        String role = authorization.get().getAuthName().toUpperCase();
        return !role.equals("VENDOR") && !role.equals("EMPLOYEE") && !role.equals("PURCHASE_DEPT");
    }
}
