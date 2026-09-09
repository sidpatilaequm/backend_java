package com.example.multimedia.file_upload_api.entity;

import com.example.multimedia.file_upload_api.enums.ExcelReportType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Saved "this Excel column feeds this DB column" config for one of the 5 SAP report excels (see
 * ExcelReportType). One row per report type. No watcher/import execution reads this yet — it
 * only backs the admin mapping screen for now.
 */
@Entity
@Table(name = "excel_import_mappings")
@Getter
@Setter
public class ExcelImportMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, unique = true, length = 30)
    private ExcelReportType reportType;

    @Column(name = "sheet_name", length = 120)
    private String sheetName;

    @Column(name = "header_row", nullable = false)
    private Integer headerRow = 5;

    /** JSON array of {"excelColumn": "...", "dbColumn": "..."}. */
    @Lob
    @Column(name = "mapping_json", columnDefinition = "LONGTEXT", nullable = false)
    private String mappingJson = "[]";

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
