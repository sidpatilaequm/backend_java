package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {

    @Autowired
    private SuperAdminService superAdminService;

    @PostMapping("/register")
    public ResponseEntity<SuperAdmin> registerSuperAdmin(@RequestBody SuperAdmin superAdmin) {
        return ResponseEntity.ok(superAdminService.registerSuperAdmin(superAdmin));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginSuperAdmin(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(superAdminService.loginSuperAdmin(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuperAdmin> getSuperAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminService.getSuperAdminById(id));
    }

    // Inner class for login request
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
} 