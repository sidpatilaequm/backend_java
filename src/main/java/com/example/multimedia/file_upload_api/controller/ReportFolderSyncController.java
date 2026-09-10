package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.ReportFolderSync;
import com.example.multimedia.file_upload_api.repository.ReportFolderSyncRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import com.example.multimedia.file_upload_api.service.ReportFolderSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin visibility/control for the FolderIt watchers backing the excel-to-table report types --
 * status of each report's last check, enable/disable, and a manual "run now" for testing without
 * waiting for the interval to elapse.
 */
@RestController
@RequestMapping("/api/admin/report-folder-sync")
public class ReportFolderSyncController {

    private final ReportFolderSyncRepository repository;
    private final ReportFolderSyncService syncService;
    private final AdminAuthChecker adminAuthChecker;

    public ReportFolderSyncController(ReportFolderSyncRepository repository,
                                       ReportFolderSyncService syncService,
                                       AdminAuthChecker adminAuthChecker) {
        this.repository = repository;
        this.syncService = syncService;
        this.adminAuthChecker = adminAuthChecker;
    }

    @GetMapping
    public ResponseEntity<List<ReportFolderSync>> list() {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(repository.findAll());
    }

    @PatchMapping("/{reportType}")
    public ResponseEntity<ReportFolderSync> update(@PathVariable String reportType, @RequestBody Map<String, Object> payload) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        ReportFolderSync config = repository.findByReportType(reportType).orElse(null);
        if (config == null) return ResponseEntity.notFound().build();
        if (payload.containsKey("enabled")) config.setEnabled(Boolean.parseBoolean(payload.get("enabled").toString()));
        if (payload.containsKey("intervalMinutes")) config.setIntervalMinutes(Integer.parseInt(payload.get("intervalMinutes").toString()));
        if (payload.containsKey("folderitFolderUid")) config.setFolderitFolderUid(payload.get("folderitFolderUid").toString());
        return ResponseEntity.ok(repository.save(config));
    }

    @PostMapping("/{reportType}/run")
    public ResponseEntity<?> runNow(@PathVariable String reportType) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        ReportFolderSync config = repository.findByReportType(reportType).orElse(null);
        if (config == null) return ResponseEntity.notFound().build();
        try {
            syncService.runSync(config);
            return ResponseEntity.ok(repository.findByReportType(reportType).orElse(config));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
