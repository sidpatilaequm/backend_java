package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.UploadResponse;
import com.example.multimedia.file_upload_api.entity.VendorMaster;
import com.example.multimedia.file_upload_api.repository.VendorMasterRepository;
import com.example.multimedia.file_upload_api.service.VendorMasterExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/vendor-master")
@RequiredArgsConstructor
public class SAPVendorMasterController {

    private final VendorMasterExcelService vendorMasterExcelService;
    private final VendorMasterRepository vendorMasterRepository;
    private final UserDetailRepository userDetailRepository;
    private final CurrentUserService currentUserService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadVendorMaster(@RequestBody com.example.multimedia.file_upload_api.dto.VendorMasterDto dto) {
        try {
            UploadResponse response = vendorMasterExcelService.uploadVendorMaster(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(UploadResponse.builder()
                    .status("FAILED: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<VendorMaster>> getAllVendorMasters() {
        try {
            Long currentAdminId = currentUserService.getCurrentSuperAdminId();
            List<VendorMaster> allVendorMasters = vendorMasterRepository.findAll();
            List<VendorMaster> filtered = allVendorMasters.stream()
                    .filter(vm -> {
                        if (vm.getEmail() == null || vm.getEmail().isEmpty()) {
                            return false;
                        }
                        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(vm.getEmail());
                        if (userOpt.isEmpty()) {
                            return false;
                        }
                        UserDetail user = userOpt.get();
                        return user.getSuperAdmin() != null && user.getSuperAdmin().getSuperAdminId().equals(currentAdminId);
                    })
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    @GetMapping("/user-data")
    public ResponseEntity<?> getVendorMasterForLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        String email = authentication.getName();
        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            List<VendorMaster> vendorMasters = vendorMasterRepository.findByEmail(email);
            return ResponseEntity.ok(vendorMasters);
        }

        return ResponseEntity.status(404).body(Map.of("error", "User not found"));
    }
}
