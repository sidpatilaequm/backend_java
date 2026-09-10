package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tracks each excel file seen in a watched FolderIt folder, keyed by (reportType, fileUid).
 * last_processed_updated_at is FolderIt's own file updatedAt (epoch seconds) at the time it was
 * last imported -- if the file's current updatedAt is newer, it's been edited (new/changed rows)
 * since and gets re-processed; if the same, it's skipped.
 */
@Entity
@Table(name = "report_folder_sync_file", uniqueConstraints = {
    @UniqueConstraint(name = "uk_report_file", columnNames = {"report_type", "file_uid"})
})
@Getter
@Setter
public class ReportFolderSyncFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_type", nullable = false, length = 30)
    private String reportType;

    @Column(name = "file_uid", nullable = false, length = 100)
    private String fileUid;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "last_processed_updated_at")
    private Long lastProcessedUpdatedAt;

    @Column(name = "last_processed_at")
    private LocalDateTime lastProcessedAt;

    @Column(name = "rows_processed", nullable = false)
    private int rowsProcessed = 0;

    @Column(name = "last_error", length = 1000)
    private String lastError;
}
