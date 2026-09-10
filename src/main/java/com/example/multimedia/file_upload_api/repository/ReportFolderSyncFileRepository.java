package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ReportFolderSyncFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportFolderSyncFileRepository extends JpaRepository<ReportFolderSyncFile, Long> {
    Optional<ReportFolderSyncFile> findByReportTypeAndFileUid(String reportType, String fileUid);
}
