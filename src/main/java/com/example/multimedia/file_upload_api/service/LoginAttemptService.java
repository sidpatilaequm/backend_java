package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.LoginAttempt;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.LoginAttemptRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Records password/Microsoft/Google sign-in attempts, successful or not. Never blocks the actual
 * sign-in flow it's instrumenting — record() swallows its own failures, same defensive pattern
 * AuditLogService uses around its own writes.
 */
@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    private final LoginAttemptRepository loginAttemptRepository;
    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository,
                                SuperAdminRepository superAdminRepository,
                                UserDetailRepository userDetailRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
    }

    public void record(String email, String method, boolean success, String failureReason) {
        try {
            LoginAttempt attempt = new LoginAttempt();
            attempt.setEmail(email);
            attempt.setMethod(method);
            attempt.setSuccess(success);
            attempt.setFailureReason(failureReason);
            attempt.setSuperAdmin(resolveTenant(email));
            loginAttemptRepository.save(attempt);
        } catch (Exception e) {
            logger.warn("Failed to record login attempt (method={}): {}", method, e.getMessage());
        }
    }

    // Left null when the email matches no account anywhere — see LoginAttempt's javadoc for why.
    private SuperAdmin resolveTenant(String email) {
        if (email == null) return null;
        return superAdminRepository.findByEmail(email)
                .or(() -> userDetailRepository.findByEmail(email).map(UserDetail::getSuperAdmin))
                .orElse(null);
    }
}
