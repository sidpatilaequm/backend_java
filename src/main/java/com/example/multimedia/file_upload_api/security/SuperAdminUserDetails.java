package com.example.multimedia.file_upload_api.security;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class SuperAdminUserDetails implements UserDetails {
    private final SuperAdmin superAdmin;

    public SuperAdminUserDetails(SuperAdmin superAdmin) {
        this.superAdmin = superAdmin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("super_admin"));
    }

    @Override
    public String getPassword() {
        return superAdmin.getPassword();
    }

    @Override
    public String getUsername() {
        return superAdmin.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return superAdmin.getIsActive();
    }

    public SuperAdmin getSuperAdmin() {
        return superAdmin;
    }
} 