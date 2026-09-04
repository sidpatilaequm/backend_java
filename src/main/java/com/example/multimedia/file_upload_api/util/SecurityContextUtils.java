package com.example.multimedia.file_upload_api.util;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityContextUtils {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository userAuthenticationRepository;

    public boolean isCurrentUserVendor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String email = authentication.getName();
        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get().getUserType() == com.example.multimedia.file_upload_api.enums.UserType.VENDOR;
        }
        return false;
    }

    public Long getCurrentVendorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        String email = authentication.getName();
        
        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            UserDetail user = userOpt.get();
            
            if (user.getCompany() != null) {
                return user.getCompany().getCompanyId();
            }
            
            Optional<com.example.multimedia.file_upload_api.entity.UserAuthentication> userAuthOpt = userAuthenticationRepository.findByUserId(user.getUserId());
            if (userAuthOpt.isPresent()) {
                return userAuthOpt.get().getUserAuthenticationId();
            } else {
                throw new RuntimeException("Vendor ID (Company ID) not found for the current user.");
            }
        }
        throw new RuntimeException("Vendor user not found.");
    }

    public Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Admin is not authenticated");
        }
        String email = authentication.getName();

        Optional<SuperAdmin> adminOpt = superAdminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            return adminOpt.get().getSuperAdminId();
        }
        throw new RuntimeException("Admin user not found.");
    }
}
