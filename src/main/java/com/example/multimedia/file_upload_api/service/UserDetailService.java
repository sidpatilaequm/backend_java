package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.UserDetailDTO;
import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.multimedia.file_upload_api.dto.UserCreationRequestDTO;
import com.example.multimedia.file_upload_api.entity.Employee;
import com.example.multimedia.file_upload_api.entity.Department;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.repository.EmployeeRepository;
import com.example.multimedia.file_upload_api.repository.DepartmentRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserDetailService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AdminAuthChecker adminAuthChecker;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    private void requireAdmin() {
        if (!adminAuthChecker.isAdmin()) {
            throw new SecurityException("Admin access required for this action.");
        }
    }





    
    private SuperAdmin getEffectiveSuperAdmin() {
        try {
            return currentUserService.getCurrentSuperAdmin();
        } catch (Exception e) {
            try {
                UserDetail user = currentUserService.getCurrentUser();
                if (user.getSuperAdmin() != null) {
                    return user.getSuperAdmin();
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
        throw new RuntimeException("Could not determine SuperAdmin context for the current user.");
    }

    @Transactional
    public List<Map<String, Object>> getUsersForCurrentAdmin() {
        requireAdmin();
        SuperAdmin currentSuperAdmin = getEffectiveSuperAdmin();
        List<UserDetail> users = userDetailRepository.findBySuperAdmin(currentSuperAdmin);
        
        return users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getUserId());
            map.put("email", u.getEmail());
            map.put("firstName", u.getFirstName());
            map.put("lastName", u.getLastName());
            map.put("phoneNumber", u.getPhoneNumber());
            map.put("isActive", u.getIsActive());
            map.put("role", u.getUserType() != null ? u.getUserType().name() : "EMPLOYEE");
            
            // Check if employee exists
            Optional<Employee> empOpt = employeeRepository.findByUserDetail_UserId(u.getUserId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                map.put("employeeCode", emp.getEmployeeCode());
                if (emp.getDepartment() != null) {
                    map.put("deptCode", emp.getDepartment().getDeptCode());
                    map.put("deptName", emp.getDepartment().getDeptName());
                }
            }
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createEmployeeUser(UserCreationRequestDTO dto) {
        // Was reachable by any authenticated user (vendor, employee, ...) via getEffectiveSuperAdmin's
        // fallback to the caller's own tenant — meaning a plain employee could create an
        // ADMINISTRATOR account for themselves within their own company.
        requireAdmin();
        SuperAdmin currentSuperAdmin = getEffectiveSuperAdmin();

        if (userDetailRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        UserType userType;
        try {
            userType = UserType.valueOf(dto.getRole().toUpperCase());
        } catch (Exception e) {
            userType = UserType.EMPLOYEE;
        }

        UserDetail userDetail = new UserDetail();
        userDetail.setEmail(dto.getEmail());
        userDetail.setPassword(passwordEncoder.encode(dto.getPassword()));
        userDetail.setFirstName(dto.getFirstName());
        userDetail.setLastName(dto.getLastName());
        userDetail.setPhoneNumber(dto.getPhoneNumber());
        userDetail.setSuperAdmin(currentSuperAdmin);
        userDetail.setUserType(userType);
        userDetail.setIsActive(true);
        userDetail.setSignupDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        userDetail = userDetailRepository.save(userDetail);
        
        // Default AuthKey mapping logic based on role. Was missing PURCHASE_DEPT/APPROVER —
        // both fell through to "employee" silently, giving that user the wrong permission link
        // with no error (PermissionDataInitializer now seeds Authorization rows for both).
        String authKeyStr = "employee"; // default
        if (userType == UserType.ADMINISTRATOR || userType == UserType.SUPER_ADMIN) authKeyStr = "administrator";
        else if (userType == UserType.PROCUREMENT_MANAGER) authKeyStr = "procurement_manager";
        else if (userType == UserType.PURCHASE_DEPT) authKeyStr = "purchase_dept";
        else if (userType == UserType.APPROVER) authKeyStr = "approver";


        Optional<Authorization> authOpt = authorizationRepository.findByAuthKeyIgnoreCase(authKeyStr);
        if (authOpt.isPresent()) {
            UserAuthentication userAuth = new UserAuthentication();
            userAuth.setUserId(userDetail.getUserId());
            userAuth.setAuthKey(String.valueOf(authOpt.get().getAuthId()));
            userAuth.setIsActive(true);
            userAuthenticationRepository.save(userAuth);
        }
        
        // Create Employee Profile
        Employee employee = new Employee();
        String randomCode = "EMP-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        employee.setEmployeeCode(randomCode);
        employee.setName(dto.getFirstName() + " " + (dto.getLastName() != null ? dto.getLastName() : ""));
        employee.setEmail(dto.getEmail());
        employee.setUserDetail(userDetail);
        
        if (dto.getDeptCode() != null && !dto.getDeptCode().trim().isEmpty()) {
            Optional<Department> deptOpt = departmentRepository.findById(dto.getDeptCode());
            if (deptOpt.isPresent()) {
                employee.setDepartment(deptOpt.get());
            }
        }
        
        employeeRepository.save(employee);

        auditLogService.record(currentSuperAdmin, "USER_CREATED", userDetail, List.of(
                new AuditLogService.FieldChange("email", null, dto.getEmail()),
                new AuditLogService.FieldChange("role", null, userType.name())
        ), false);

        // The generated password used to only ever reach the admin who created this account, in
        // the API response — never the actual user. sendSimpleEmailToUserId already existed
        // (used for other notifications) but was never called from account creation.
        try {
            emailService.sendSimpleEmailToUserId(userDetail.getUserId(),
                    "Your account was created",
                    "Hi " + dto.getFirstName() + ",\n\n"
                            + "An account was created for you.\n\n"
                            + "Email: " + dto.getEmail() + "\n"
                            + "Temporary password: " + dto.getPassword() + "\n\n"
                            + "Sign in and change this password as soon as you can.");
        } catch (Exception e) {
            // Don't fail account creation over a delivery problem — the admin still sees the
            // password in the response and can pass it along another way.
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userDetail.getUserId());
        response.put("employeeCode", employee.getEmployeeCode());
        response.put("message", "User created successfully");
        return response;
    }

    @Transactional
    public Map<String, Object> registerUser(UserDetailDTO userDetailDTO) {
        // This endpoint used to be fully public (no auth at all) and trusted whatever
        // superAdminId/authKey the caller sent — an anonymous caller could plant an account with
        // any role in any tenant and get the generated password back in the response. Now:
        // admin-gated, and the tenant is always the caller's own, never client-supplied.
        requireAdmin();
        SuperAdmin superAdmin = getEffectiveSuperAdmin();

        Optional<UserDetail> emailUser = userDetailRepository.findByEmail(userDetailDTO.getEmail());
        // findByPhoneNumber(null) matches every row with a NULL phone number (Spring Data turns a
        // null parameter into "IS NULL"), and throws IncorrectResultSizeDataAccessException the
        // moment more than one exists — a real, reachable bug for the (very common) case of
        // registering someone with no phone number supplied. Only look up by phone when one
        // was actually given.
        Optional<UserDetail> phoneUser = (userDetailDTO.getPhoneNumber() != null && !userDetailDTO.getPhoneNumber().isBlank())
                ? userDetailRepository.findByPhoneNumber(userDetailDTO.getPhoneNumber())
                : Optional.empty();

        // Case 1: Email or phone exists and belong to different users
        if (emailUser.isPresent() && phoneUser.isPresent() && !emailUser.get().getUserId().equals(phoneUser.get().getUserId())) {
            throw new RuntimeException("Email and phone number belong to different users.");
        }

        // An existing match in a different tenant would otherwise let this endpoint attach a new
        // role to someone else's account.
        Optional<UserDetail> existingMatch = emailUser.isPresent() ? emailUser : phoneUser;
        if (existingMatch.isPresent()) {
            UserDetail existing = existingMatch.get();
            if (existing.getSuperAdmin() == null
                    || !existing.getSuperAdmin().getSuperAdminId().equals(superAdmin.getSuperAdminId())) {
                throw new SecurityException("That email or phone number belongs to an account outside your organization.");
            }
        }

        UserDetail userDetail;
        boolean isNewUser = existingMatch.isEmpty();

        String rawPassword = "********";

        // Case 2: Use existing user if found
        if (emailUser.isPresent()) {
            userDetail = emailUser.get();
        } else if (phoneUser.isPresent()) {
            userDetail = phoneUser.get();
        } else {
            // Case 3: Create new user
            userDetail = new UserDetail();
            userDetail.setEmail(userDetailDTO.getEmail());
            
            // Set random password with name included
            rawPassword = com.example.multimedia.file_upload_api.utils.PasswordUtils.generateRandomPassword(userDetailDTO.getFirstName());
            userDetail.setPassword(passwordEncoder.encode(rawPassword)); 
            
            userDetail.setSignupDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            userDetail.setIsActive(true);
            
            // Set optional fields if provided
            if (userDetailDTO.getFirstName() != null) {
            userDetail.setFirstName(userDetailDTO.getFirstName());
            }
            if (userDetailDTO.getLastName() != null) {
            userDetail.setLastName(userDetailDTO.getLastName());
            }
            if (userDetailDTO.getPhoneNumber() != null) {
            userDetail.setPhoneNumber(userDetailDTO.getPhoneNumber());
            }
            
            userDetail.setSuperAdmin(superAdmin);
            userDetail = userDetailRepository.save(userDetail);
        }

        // Get Authorization
        Authorization authorization = authorizationRepository.findByAuthKey(userDetailDTO.getAuthKey())
                .orElseThrow(() -> new RuntimeException("Invalid authKey"));

        // Check if user already has this role
        boolean roleExists = userAuthenticationRepository.existsByUserIdAndAuthKey(userDetail, authorization);
        if (roleExists) {
            throw new RuntimeException("User already registered with this role.");
        }

        // Save new role mapping
        UserAuthentication userAuth = new UserAuthentication();
        userAuth.setUserId(userDetail.getUserId());
        userAuth.setAuthKey(String.valueOf(authorization.getAuthId()));
        userAuth.setIsActive(true);
        userAuthenticationRepository.save(userAuth);

        if (isNewUser) {
            auditLogService.record(superAdmin, "USER_CREATED", userDetail, List.of(
                    new AuditLogService.FieldChange("email", null, userDetail.getEmail()),
                    new AuditLogService.FieldChange("role", null, authorization.getAuthName())
            ), false);

            try {
                emailService.sendSimpleEmailToUserId(userDetail.getUserId(),
                        "Your account was created",
                        "Hi " + (userDetail.getFirstName() != null ? userDetail.getFirstName() : "") + ",\n\n"
                                + "An account was created for you as " + authorization.getAuthName() + ".\n\n"
                                + "Email: " + userDetail.getEmail() + "\n"
                                + "Temporary password: " + rawPassword + "\n\n"
                                + "Sign in and change this password as soon as you can.");
            } catch (Exception e) {
                // Don't fail account creation over a delivery problem.
            }
        } else {
            auditLogService.record(superAdmin, "ROLE_ASSIGNED", userDetail, List.of(
                    new AuditLogService.FieldChange("role", null, authorization.getAuthName())
            ), false);
        }

        // Response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userDetail.getUserId());
        response.put("email", userDetail.getEmail());
        response.put("firstName", userDetail.getFirstName());
        response.put("lastName", userDetail.getLastName());
        response.put("phoneNumber", userDetail.getPhoneNumber());
        response.put("isActive", userDetail.getIsActive());
        response.put("authId", authorization.getAuthId());
        response.put("superAdminId", superAdmin.getSuperAdminId());
        response.put("password", rawPassword); // Return the generated password so the user can see it
        response.put("message", "User registered successfully with role: " + authorization.getAuthName());

        return response;
    }

    // Not exposed directly — every caller above needs the admin+tenant check in getUserById below,
    // this only exists so updateUser/deactivateUser (which do their own save afterward) don't run
    // the lookup twice.
    private UserDetail findByIdRaw(Long userId) {
        return userDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Was: any authenticated user, in any tenant, could fetch any user by id.
    public UserDetail getUserById(Long userId) {
        requireAdmin();
        UserDetail user = findByIdRaw(userId);
        assertSameTenant(user);
        return user;
    }

    // Was: no admin check, no tenant check — any authenticated user could edit (including set the
    // password of) any user in any tenant.
    @Transactional
    public UserDetail updateUser(Long userId, UserDetailDTO userDetailDTO) {
        requireAdmin();
        UserDetail existingUser = findByIdRaw(userId);
        assertSameTenant(existingUser);

        // Captured before mutation, and only turned into a logged diff if the new value actually
        // differs — the DTO field being non-null doesn't mean the value changed (e.g. the form
        // resubmits the existing phone number unchanged), and logging every no-op call would fill
        // the audit trail with noise like "phone: 987654 -> 987654".
        java.util.List<AuditLogService.FieldChange> changes = new java.util.ArrayList<>();
        if (userDetailDTO.getFirstName() != null && !userDetailDTO.getFirstName().equals(existingUser.getFirstName())) {
            changes.add(new AuditLogService.FieldChange("firstName", existingUser.getFirstName(), userDetailDTO.getFirstName()));
            existingUser.setFirstName(userDetailDTO.getFirstName());
        }
        if (userDetailDTO.getLastName() != null && !userDetailDTO.getLastName().equals(existingUser.getLastName())) {
            changes.add(new AuditLogService.FieldChange("lastName", existingUser.getLastName(), userDetailDTO.getLastName()));
            existingUser.setLastName(userDetailDTO.getLastName());
        }
        if (userDetailDTO.getPhoneNumber() != null && !userDetailDTO.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
            changes.add(new AuditLogService.FieldChange("phoneNumber", existingUser.getPhoneNumber(), userDetailDTO.getPhoneNumber()));
            existingUser.setPhoneNumber(userDetailDTO.getPhoneNumber());
        }

        boolean passwordReset = userDetailDTO.getPassword() != null && !userDetailDTO.getPassword().isEmpty();
        if (passwordReset) {
            existingUser.setPassword(passwordEncoder.encode(userDetailDTO.getPassword()));
        }

        existingUser = userDetailRepository.save(existingUser);

        if (!changes.isEmpty() || passwordReset) {
            auditLogService.record(getEffectiveSuperAdmin(), "USER_UPDATED", existingUser, changes, passwordReset);
        }

        // This is now also where "reset a user's password" happens (the old, unauthenticated
        // /forgot-password endpoint — any logged-in user could reset anyone's password just by
        // knowing their email — was removed rather than fixed in place; an admin-initiated reset
        // through this already-gated, already-tenant-checked path is the replacement).
        if (passwordReset) {
            try {
                emailService.sendSimpleEmailToUserId(existingUser.getUserId(),
                        "Your password was reset",
                        "Hi " + (existingUser.getFirstName() != null ? existingUser.getFirstName() : "") + ",\n\n"
                                + "An administrator reset your password.\n\n"
                                + "New temporary password: " + userDetailDTO.getPassword() + "\n\n"
                                + "Sign in and change it as soon as you can. If you didn't expect "
                                + "this, contact your administrator.");
            } catch (Exception e) {
                // Don't fail the update over a delivery problem.
            }
        }

        return existingUser;
    }

    // Was: no admin check, no tenant check — any authenticated user could deactivate any user in
    // any tenant.
    @Transactional
    public void deactivateUser(Long userId) {
        requireAdmin();
        UserDetail user = findByIdRaw(userId);
        assertSameTenant(user);
        user.setIsActive(false);
        userDetailRepository.save(user);

        auditLogService.record(getEffectiveSuperAdmin(), "USER_DEACTIVATED", user, List.of(
                new AuditLogService.FieldChange("isActive", "true", "false")
        ), false);
    }

    private void assertSameTenant(UserDetail target) {
        SuperAdmin currentSuperAdmin = getEffectiveSuperAdmin();
        if (target.getSuperAdmin() == null
                || !target.getSuperAdmin().getSuperAdminId().equals(currentSuperAdmin.getSuperAdminId())) {
            // Same message as "not found" deliberately — this shouldn't confirm to a caller in one
            // tenant that a given id exists in another.
            throw new RuntimeException("User not found");
        }
    }
} 