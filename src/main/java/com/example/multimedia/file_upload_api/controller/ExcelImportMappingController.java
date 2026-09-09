package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ExcelImportMappingDto;
import com.example.multimedia.file_upload_api.dto.ExcelInspectResultDto;
import com.example.multimedia.file_upload_api.dto.ExcelTargetColumnDto;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.enums.ExcelReportType;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import com.example.multimedia.file_upload_api.service.ExcelImportMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Admin screen: for each of the 5 SAP report excels, which Excel column feeds which column of
 * that report's real target table (ExcelReportType). Config only -- saving a mapping here does
 * not import or watch anything yet.
 */
@RestController
@RequestMapping("/api/admin/excel-mappings")
public class ExcelImportMappingController {

    private final ExcelImportMappingService service;
    private final AdminAuthChecker adminAuthChecker;
    private final UserDetailRepository userDetailRepository;

    public ExcelImportMappingController(ExcelImportMappingService service, AdminAuthChecker adminAuthChecker,
                                         UserDetailRepository userDetailRepository) {
        this.service = service;
        this.adminAuthChecker = adminAuthChecker;
        this.userDetailRepository = userDetailRepository;
    }

    @GetMapping
    public ResponseEntity<List<ExcelImportMappingDto>> listAll() {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{reportType}")
    public ResponseEntity<ExcelImportMappingDto> get(@PathVariable ExcelReportType reportType) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(service.get(reportType));
    }

    @GetMapping("/{reportType}/target-columns")
    public ResponseEntity<List<ExcelTargetColumnDto>> targetColumns(@PathVariable ExcelReportType reportType) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(service.targetColumns(reportType));
    }

    @PostMapping(value = "/{reportType}/inspect", consumes = "multipart/form-data")
    public ResponseEntity<ExcelInspectResultDto> inspect(@PathVariable ExcelReportType reportType,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "sheet", required = false) String sheet,
                                                           @RequestParam(value = "headerRow", defaultValue = "5") int headerRow) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(service.inspect(file, sheet, headerRow));
    }

    @PutMapping("/{reportType}")
    public ResponseEntity<ExcelImportMappingDto> save(@PathVariable ExcelReportType reportType,
                                                        @RequestBody ExcelImportMappingDto dto) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        Long adminUserId = currentUserId();
        return ResponseEntity.ok(service.save(reportType, dto, adminUserId));
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserDetail> user = userDetailRepository.findByEmail(email);
        return user.map(UserDetail::getUserId).orElse(null);
    }
}
