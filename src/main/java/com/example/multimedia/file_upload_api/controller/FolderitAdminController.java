package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.FolderitSyncConfig;
import com.example.multimedia.file_upload_api.repository.FolderitSyncConfigRepository;
import com.example.multimedia.file_upload_api.security.AdminAuthChecker;
import com.example.multimedia.file_upload_api.service.FolderitBatchJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Every endpoint here is gated by AdminAuthChecker — the merged-in branch left these with no
// authorization check at all (just .anyRequest().authenticated() from SecurityConfig), which
// meant any logged-in vendor or employee, not just an admin, could read/change the FolderIt
// sync config or trigger a sync run.
@RestController
@RequestMapping("/api/admin/folderit")
public class FolderitAdminController {

    private final FolderitSyncConfigRepository configRepository;
    private final FolderitBatchJobService batchJobService;
    private final AdminAuthChecker adminAuthChecker;

    public FolderitAdminController(FolderitSyncConfigRepository configRepository,
                                   FolderitBatchJobService batchJobService,
                                   AdminAuthChecker adminAuthChecker) {
        this.configRepository = configRepository;
        this.batchJobService = batchJobService;
        this.adminAuthChecker = adminAuthChecker;
    }

    @GetMapping("/config")
    public ResponseEntity<FolderitSyncConfig> getConfig() {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        FolderitSyncConfig config = configRepository.findById(1L).orElse(new FolderitSyncConfig());
        return ResponseEntity.ok(config);
    }

    @PostMapping("/config")
    public ResponseEntity<FolderitSyncConfig> updateConfig(@RequestBody Map<String, Object> payload) {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        FolderitSyncConfig config = configRepository.findById(1L).orElse(new FolderitSyncConfig());

        if (payload.containsKey("isEnabled")) {
            config.setEnabled(Boolean.parseBoolean(payload.get("isEnabled").toString()));
        }
        if (payload.containsKey("intervalMinutes")) {
            config.setIntervalMinutes(Integer.parseInt(payload.get("intervalMinutes").toString()));
        }

        configRepository.save(config);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/run-now")
    public ResponseEntity<?> runNow() {
        if (!adminAuthChecker.isAdmin()) return ResponseEntity.status(403).build();
        FolderitSyncConfig config = configRepository.findById(1L).orElse(new FolderitSyncConfig());

        // Run synchronously so the user knows when it finishes
        batchJobService.runBatchJob(config);

        // Fetch updated config with latest status
        FolderitSyncConfig updatedConfig = configRepository.findById(1L).orElse(config);

        return ResponseEntity.ok(Map.of(
            "message", "Job executed manually.",
            "status", updatedConfig.getLastRunStatus(),
            "filesProcessed", updatedConfig.getFilesProcessedLastRun()
        ));
    }
}
