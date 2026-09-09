package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ExcelImportMapping;
import com.example.multimedia.file_upload_api.enums.ExcelReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExcelImportMappingRepository extends JpaRepository<ExcelImportMapping, Long> {
    Optional<ExcelImportMapping> findByReportType(ExcelReportType reportType);
}
