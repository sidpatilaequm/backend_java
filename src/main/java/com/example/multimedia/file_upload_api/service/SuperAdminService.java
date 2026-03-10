package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SuperAdminService {

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    public SuperAdmin registerSuperAdmin(SuperAdmin superAdmin) {
        if (superAdminRepository.existsByEmail(superAdmin.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Encode the password before saving
        superAdmin.setPassword(passwordEncoder.encode(superAdmin.getPassword()));
        return superAdminRepository.save(superAdmin);
    }

    public Map<String, Object> loginSuperAdmin(String email, String password) {
        // Delegate credential validation to Spring Security so both SuperAdmin and UserDetail can log in.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal);

        // First, check if this is a super admin account.
        Optional<SuperAdmin> superAdminOpt = superAdminRepository.findByEmail(principal.getUsername());
        if (superAdminOpt.isPresent()) {
            SuperAdmin superAdmin = superAdminOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", superAdmin.getRole());
            response.put("superAdminId", superAdmin.getSuperAdminId());
            response.put("email", superAdmin.getEmail());
            response.put("firstName", superAdmin.getFirstName());
            response.put("lastName", superAdmin.getLastName());
            response.put("phoneNumber", superAdmin.getPhoneNumber());
            return response;
        }

        // Otherwise, treat it as a regular user login.
        return userDetailRepository.findByEmail(principal.getUsername())
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("role", "USER");
                    response.put("userId", user.getUserId());
                    response.put("email", user.getEmail());
                    response.put("firstName", user.getFirstName());
                    response.put("lastName", user.getLastName());
                    response.put("phoneNumber", user.getPhoneNumber());

                    // Attach authorization info (if present)
                    userAuthenticationRepository.findByUserId(user.getUserId()).ifPresent(userAuth -> {
                        authorizationRepository.findByAuthKey(userAuth.getAuthKey()).ifPresent(auth -> {
                            response.put("authId", auth.getAuthId());
                            response.put("authName", auth.getAuthName());
                            response.put("role", auth.getAuthName());
                        });
                    });

                    // Fall back to company_details authKey if no role was set from user_authentication
                    if (!response.containsKey("authId")) {
                        companyDetailsRepository.findByUserUserId(user.getUserId()).stream().findFirst().ifPresent(company -> {
                            authorizationRepository.findByAuthKeyIgnoreCase(company.getAuthKey()).ifPresent(auth -> {
                                response.put("authId", auth.getAuthId());
                                response.put("authName", auth.getAuthName());
                                response.put("role", auth.getAuthName());
                            });
                        });
                    }

                    return response;
                })
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    }

    public SuperAdmin getSuperAdminById(Long id) {
        return superAdminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Super Admin not found"));
    }
} 