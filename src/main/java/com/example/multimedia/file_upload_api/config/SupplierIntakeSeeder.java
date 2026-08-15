package com.example.multimedia.file_upload_api.config;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Idempotently creates the "system" SuperAdmin + intake UserDetail used as
 * the technical submitter_id when SupplierRegistrationService submits a
 * self-serve applicant's request into WorkFlow's "Vendor Approval" workflow
 * — WorkFlow's own submit endpoint requires an existing, active user_details
 * row, but a prospective supplier has no account yet. This account has no
 * UserAuthentication row, so it can't actually log in — it only exists to
 * satisfy that foreign key.
 */
@Component
public class SupplierIntakeSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SupplierIntakeSeeder.class);
    public static final String INTAKE_EMAIL = "supplier-intake@internal";
    private static final String SYSTEM_ADMIN_EMAIL = "system@internal";

    private final SuperAdminRepository superAdminRepository;
    private final UserDetailRepository userDetailRepository;
    private final PasswordEncoder passwordEncoder;

    public SupplierIntakeSeeder(SuperAdminRepository superAdminRepository,
                                 UserDetailRepository userDetailRepository,
                                 PasswordEncoder passwordEncoder) {
        this.superAdminRepository = superAdminRepository;
        this.userDetailRepository = userDetailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        SuperAdmin systemAdmin = superAdminRepository.findByEmail(SYSTEM_ADMIN_EMAIL).orElseGet(() -> {
            SuperAdmin admin = new SuperAdmin();
            admin.setEmail(SYSTEM_ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            admin.setFirstName("System");
            admin.setLastName("Account");
            admin.setRole("SUPER_ADMIN");
            admin.setIsActive(true);
            SuperAdmin saved = superAdminRepository.save(admin);
            logger.info("Created system SuperAdmin ({})", SYSTEM_ADMIN_EMAIL);
            return saved;
        });

        if (userDetailRepository.findByEmail(INTAKE_EMAIL).isEmpty()) {
            UserDetail intake = new UserDetail();
            intake.setSuperAdmin(systemAdmin);
            intake.setEmail(INTAKE_EMAIL);
            intake.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            intake.setFirstName("Supplier");
            intake.setLastName("Intake");
            intake.setUserType(UserType.VENDOR);
            intake.setIsActive(true);
            userDetailRepository.save(intake);
            logger.info("Created supplier-intake service account ({})", INTAKE_EMAIL);
        }
    }
}
