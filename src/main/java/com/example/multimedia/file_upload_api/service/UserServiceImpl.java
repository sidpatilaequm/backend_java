package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Override
    public boolean isSuperAdmin(Long userId) {
        UserAuthentication userAuth = userAuthenticationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User authentication not found"));
        
        if (!userAuth.getIsActive()) {
            throw new RuntimeException("User authentication is not active");
        }
        
        return "SUPER_ADMIN".equals(userAuth.getAuthKey());
    }

    @Override
    public boolean isVendor(Long userId) {
        UserAuthentication userAuth = userAuthenticationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User authentication not found"));
        
        if (!userAuth.getIsActive()) {
            throw new RuntimeException("User authentication is not active");
        }
        
        return "VENDOR".equals(userAuth.getAuthKey());
    }

    @Override
    public boolean isUserActive(Long userId) {
        UserAuthentication userAuth = userAuthenticationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User authentication not found"));
        return userAuth.getIsActive();
    }
} 