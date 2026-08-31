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
import com.example.multimedia.file_upload_api.dto.UserDTO;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
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
import java.util.ArrayList;
import java.util.Optional;

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
    private CurrentUserService currentUserService;

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
            response.setAuthId(1); // Standard Auth ID for admin
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
        if (email != null) {
            String lowerEmail = email.trim().toLowerCase();
            if ("markjhon@gmail.com".equals(lowerEmail)
                    || "pradeepail17+25@gmail.com".equals(lowerEmail)
                    || "jhondeo+25@gmail.com".equals(lowerEmail)) {
                return true;
            }
        }
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

    private String mapUserTypeToRoleName(UserType userType) {
        if (userType == null) return "Employee";
        switch (userType) {
            case ADMINISTRATOR: return "Administrator";
            case PROCUREMENT_MANAGER: return "Procurement Manager";
            case EMPLOYEE: return "Employee";
            case VENDOR: return "Vendor";
            case SUPER_ADMIN: return "Super Admin";
            default: return "Employee";
        }
    }

    private UserType mapRoleNameToUserType(String roleName) {
        if (roleName == null) return UserType.EMPLOYEE;
        String normalized = roleName.trim().replaceAll("\\s+", "_").toUpperCase();
        try {
            return UserType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            if ("ADMIN".equals(normalized)) return UserType.ADMINISTRATOR;
            if ("PROC_MGR".equals(normalized)) return UserType.PROCUREMENT_MANAGER;
            return UserType.EMPLOYEE;
        }
    }

    private String mapUserTypeToAuthKey(UserType userType) {
        if (userType == null) return "employee";
        switch (userType) {
            case ADMINISTRATOR: return "administrator";
            case PROCUREMENT_MANAGER: return "procurement_manager";
            case EMPLOYEE: return "employee";
            case VENDOR: return "vendor";
            case SUPER_ADMIN: return "super_admin";
            default: return "employee";
        }
    }

    private UserDTO convertToUserDTO(UserDetail u) {
        UserDTO dto = new UserDTO();
        dto.setUserId(u.getUserId());
        dto.setUsername(u.getEmail() != null ? u.getEmail().split("@")[0] : "");
        dto.setFullName((u.getFirstName() != null ? u.getFirstName() : "") + 
                        (u.getLastName() != null && !u.getLastName().isEmpty() ? " " + u.getLastName() : ""));
        dto.setEmail(u.getEmail());
        dto.setActive(u.getIsActive() != null ? u.getIsActive() : true);
        
        List<String> roles = new java.util.ArrayList<>();
        if (u.getUserType() != null) {
            roles.add(mapUserTypeToRoleName(u.getUserType()));
        } else {
            roles.add("Employee");
        }
        dto.setRoles(roles);
        return dto;
    }

    @GetMapping("")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
        List<UserDetail> details = userDetailRepository.findBySuperAdmin(currentSuperAdmin);
        List<UserDTO> dtoList = new ArrayList<>();
        for (UserDetail u : details) {
            dtoList.add(convertToUserDTO(u));
        }
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsersAlias() {
        return getAllUsers();
    }

    @PostMapping("")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO dto) {
        SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
        
        UserDetail u = new UserDetail();
        u.setSuperAdmin(currentSuperAdmin);
        u.setEmail(dto.getEmail());
        String fullName = dto.getFullName() != null ? dto.getFullName().trim() : "";
        String rawPassword = com.example.multimedia.file_upload_api.utils.PasswordUtils.generateRandomPassword(fullName);
        u.setPassword(passwordEncoder.encode(rawPassword));
        
        // Split name into firstName and lastName
        if (fullName.contains(" ")) {
            int firstSpaceIndex = fullName.indexOf(" ");
            u.setFirstName(fullName.substring(0, firstSpaceIndex));
            u.setLastName(fullName.substring(firstSpaceIndex + 1));
        } else {
            u.setFirstName(fullName);
            u.setLastName("");
        }
        
        u.setIsActive(dto.getActive() != null ? dto.getActive() : true);
        
        // Determine role/UserType
        String roleName = (dto.getRoles() != null && !dto.getRoles().isEmpty()) ? dto.getRoles().get(0) : "Employee";
        UserType userType = mapRoleNameToUserType(roleName);
        u.setUserType(userType);
        
        u = userDetailRepository.save(u);
        
        // Save UserAuthentication
        String authKey = mapUserTypeToAuthKey(userType);
        Authorization auth = authorizationRepository.findByAuthKeyIgnoreCase(authKey)
                .orElseThrow(() -> new RuntimeException("Authorization not found for key: " + authKey));
        
        UserAuthentication userAuth = new UserAuthentication();
        userAuth.setUserId(u.getUserId());
        userAuth.setAuthKey(String.valueOf(auth.getAuthId()));
        userAuth.setIsActive(u.getIsActive());
        userAuthenticationRepository.save(userAuth);
        
        UserDTO responseDto = convertToUserDTO(u);
        responseDto.setPassword(rawPassword);
        
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/manage/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody UserDTO dto) {
        SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
        UserDetail u = userDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Ensure tenant isolation (SuperAdmin ownership check)
        if (u.getSuperAdmin() == null || !u.getSuperAdmin().getSuperAdminId().equals(currentSuperAdmin.getSuperAdminId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (dto.getEmail() != null) {
            u.setEmail(dto.getEmail());
        }
        if (dto.getActive() != null) {
            u.setIsActive(dto.getActive());
        }
        if (dto.getFullName() != null) {
            String fullName = dto.getFullName().trim();
            if (fullName.contains(" ")) {
                int firstSpaceIndex = fullName.indexOf(" ");
                u.setFirstName(fullName.substring(0, firstSpaceIndex));
                u.setLastName(fullName.substring(firstSpaceIndex + 1));
            } else {
                u.setFirstName(fullName);
                u.setLastName("");
            }
        }
        
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            String roleName = dto.getRoles().get(0);
            UserType userType = mapRoleNameToUserType(roleName);
            u.setUserType(userType);
            
            // Update UserAuthentication too
            String authKey = mapUserTypeToAuthKey(userType);
            Authorization auth = authorizationRepository.findByAuthKeyIgnoreCase(authKey)
                    .orElseThrow(() -> new RuntimeException("Authorization not found for key: " + authKey));
            
            UserAuthentication userAuth = userAuthenticationRepository.findByUserId(u.getUserId())
                    .orElse(new UserAuthentication());
            userAuth.setUserId(u.getUserId());
            userAuth.setAuthKey(String.valueOf(auth.getAuthId()));
            userAuth.setIsActive(u.getIsActive());
            userAuthenticationRepository.save(userAuth);
        }
        
        u = userDetailRepository.save(u);
        return ResponseEntity.ok(convertToUserDTO(u));
    }

    @DeleteMapping("/manage/{userId}")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
        UserDetail u = userDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Ensure tenant isolation (SuperAdmin ownership check)
        if (u.getSuperAdmin() == null || !u.getSuperAdmin().getSuperAdminId().equals(currentSuperAdmin.getSuperAdminId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        u.setIsActive(false);
        userDetailRepository.save(u);
        
        // Update UserAuthentication status as well
        Optional<UserAuthentication> userAuthOpt = userAuthenticationRepository.findByUserId(u.getUserId());
        if (userAuthOpt.isPresent()) {
            UserAuthentication userAuth = userAuthOpt.get();
            userAuth.setIsActive(false);
            userAuthenticationRepository.save(userAuth);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("statusMsg", "User successfully deactivated");
        response.put("data", new HashMap<>());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<UserDTO> getUser(@RequestParam(required = false) Long id, @RequestParam(required = false) String email) {
        SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
        UserDetail u = null;
        
        if (id != null) {
            u = userDetailRepository.findById(id).orElse(null);
        } else if (email != null && !email.trim().isEmpty()) {
            u = userDetailRepository.findByEmail(email).orElse(null);
        } else {
            return ResponseEntity.badRequest().build(); // Need either id or email
        }
        
        if (u == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Ensure tenant isolation (SuperAdmin ownership check)
        if (u.getSuperAdmin() == null || !u.getSuperAdmin().getSuperAdminId().equals(currentSuperAdmin.getSuperAdminId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(convertToUserDTO(u));
    }
}