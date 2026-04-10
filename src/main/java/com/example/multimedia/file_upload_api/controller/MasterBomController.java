package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.MasterBomResponseDto;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import com.example.multimedia.file_upload_api.service.MasterBomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bom/master")
@RequiredArgsConstructor
public class MasterBomController {

    private final MasterBomService masterBomService;
    private final CurrentUserService currentUserService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMasterBom(
            @RequestParam("file") MultipartFile file) {
        SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
        masterBomService.uploadMasterBomExcel(file, superAdmin);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Master BOM uploaded successfully"));
    }

    @GetMapping("/fetch")
    public ResponseEntity<MasterBomResponseDto> fetchBom(
            @RequestParam("fg_number") String fgNumber,
            @RequestParam(value = "file_id", required = false) Long fileId) {
        SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
        MasterBomResponseDto result = masterBomService.fetchBomForFgNumber(fgNumber, superAdmin,
                Optional.ofNullable(fileId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/files")
    public ResponseEntity<?> getAllFiles() {
        SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
        return ResponseEntity.ok(masterBomService.getAllMasterBomFiles(superAdmin));
    }
}
