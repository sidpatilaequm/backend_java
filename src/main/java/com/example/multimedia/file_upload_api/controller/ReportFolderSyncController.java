package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.ReportFolderSync;
import com.example.multimedia.file_upload_api.repository.ReportFolderSyncRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import com.example.multimedia.file_upload_api.service.FolderItService;
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
    private final FolderItService folderItService;
    private final AdminAuthChecker adminAuthChecker;

    public ReportFolderSyncController(ReportFolderSyncRepository repository,
                                       ReportFolderSyncService syncService,
                                       FolderItService folderItService,
                                       AdminAuthChecker adminAuthChecker) {
        this.repository = repository;
        this.syncService = syncService;
        this.folderItService = folderItService;
        this.adminAuthChecker = adminAuthChecker;
    }

    public record SyncStatusDto(
            String reportType, String folderitFolderUid, String folderName, boolean enabled,
            int intervalMinutes, Object lastRunAt, String lastRunStatus, int filesProcessedLastRun) {
    }

    @GetMapping
    public ResponseEntity<List<SyncStatusDto>> list() {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        List<SyncStatusDto> out = repository.findAll().stream().map(c -> new SyncStatusDto(
                c.getReportType(), c.getFolderitFolderUid(), folderItService.getFolderName(c.getFolderitFolderUid()),
                c.isEnabled(), c.getIntervalMinutes(), c.getLastRunAt(), c.getLastRunStatus(), c.getFilesProcessedLastRun()
        )).toList();
        return ResponseEntity.ok(out);
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
