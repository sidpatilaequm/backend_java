package com.example.multimedia.file_upload_api.service;

public interface UserService {
    boolean isSuperAdmin(Long userId);
    boolean isVendor(Long userId);
    boolean isUserActive(Long userId);
} 