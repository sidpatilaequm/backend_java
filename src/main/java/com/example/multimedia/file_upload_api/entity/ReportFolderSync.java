package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * One row per report type (ExcelReportType) that has a FolderIt watch enabled -- which folder to
 * check, how often, and the result of the last check. services/report-folder-sync's scheduled
 * job reads this every minute and only actually checks a folder once its interval has elapsed.
 */
@Entity
@Table(name = "report_folder_sync")
@Getter
@Setter
public class ReportFolderSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_type", nullable = false, unique = true, length = 30)
    private String reportType;

    @Column(name = "folderit_folder_uid", nullable = false, length = 100)
    private String folderitFolderUid;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "interval_minutes", nullable = false)
    private int intervalMinutes = 5;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_run_status", length = 500)
    private String lastRunStatus;

    @Column(name = "files_processed_last_run", nullable = false)
    private int filesProcessedLastRun = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
