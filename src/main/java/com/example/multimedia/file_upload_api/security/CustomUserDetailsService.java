package com.example.multimedia.file_upload_api.security;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First try to find a SuperAdmin
        Optional<SuperAdmin> superAdminOpt = superAdminRepository.findByEmail(email);
        if (superAdminOpt.isPresent()) {
            SuperAdmin superAdmin = superAdminOpt.get();
            return new SuperAdminUserDetails(superAdmin);
        }

        // If not a SuperAdmin, try to find a regular user
        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            UserDetail user = userOpt.get();
            java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
            if (user.getUserType() != null) {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getUserType().name()));
            }
            // isActive previously had no effect here — Spring's 3-arg User(...) constructor
            // defaults enabled=true regardless, so a deactivated account could still sign in.
            // The 7-arg constructor actually wires isActive through to isEnabled(), which
            // DaoAuthenticationProvider (password login) already checks automatically, and which
            // MicrosoftSsoController now checks explicitly too (SSO never goes through
            // DaoAuthenticationProvider, so it has to check this itself).
            boolean enabled = Boolean.TRUE.equals(user.getIsActive());
            return new User(user.getEmail(), user.getPassword(), enabled, true, true, true, authorities);
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    public Long getUserIdByEmail(String email) {
        // First try to find a SuperAdmin
        Optional<SuperAdmin> superAdminOpt = superAdminRepository.findByEmail(email);
        if (superAdminOpt.isPresent()) {
            return superAdminOpt.get().getSuperAdminId();
        }

        // If not a SuperAdmin, try to find a regular user
        UserDetail user = userDetailRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return user.getUserId();
    }
}