package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    // UserAuthentication.authKey stores the numeric Authorization.authId as a string, never the
    // role name — isSuperAdmin/isVendor below used to compare it directly against "SUPER_ADMIN"/
    // "VENDOR" literals, which could never match (confirmed: every writer of authKey does
    // setAuthKey(String.valueOf(authId))). Resolve the real Authorization row first, same
    // resolution AdminAuthChecker already does correctly.
    private String resolveAuthName(UserAuthentication userAuth) {
        String authKey = userAuth.getAuthKey();
        Authorization authorization;
        try {
            authorization = authorizationRepository.findById(Integer.parseInt(authKey))
                    .orElseThrow(() -> new RuntimeException("Authorization not found for id: " + authKey));
        } catch (NumberFormatException e) {
            authorization = authorizationRepository.findByAuthKeyIgnoreCase(authKey)
                    .orElseThrow(() -> new RuntimeException("Authorization not found for key: " + authKey));
        }
        return authorization.getAuthName();
    }

    @Override
    public boolean isSuperAdmin(Long userId) {
        UserAuthentication userAuth = userAuthenticationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User authentication not found"));

        if (!userAuth.getIsActive()) {
            throw new RuntimeException("User authentication is not active");
        }

        return "Super Admin".equalsIgnoreCase(resolveAuthName(userAuth));
    }

    @Override
    public boolean isVendor(Long userId) {
        UserAuthentication userAuth = userAuthenticationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User authentication not found"));

        if (!userAuth.getIsActive()) {
            throw new RuntimeException("User authentication is not active");
        }

        return "Vendor".equalsIgnoreCase(resolveAuthName(userAuth));
    }

    @Override
    public boolean isUserActive(Long userId) {
        UserAuthentication userAuth = userAuthenticationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User authentication not found"));
        return userAuth.getIsActive();
    }
} 