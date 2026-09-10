package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ReportFolderSync;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportFolderSyncRepository extends JpaRepository<ReportFolderSync, Long> {
    List<ReportFolderSync> findByEnabledTrue();
    Optional<ReportFolderSync> findByReportType(String reportType);
}
