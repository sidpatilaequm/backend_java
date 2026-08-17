package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Maps onto Form Studio's own {@code processes} table (see dynamic_questions/db/schema.sql /
 * backend/app/models.py) — same shared database, owned/authored via Form Studio's FastAPI, but
 * read directly here so the seller-facing Become-a-Supplier page and answer validation don't
 * depend on Form Studio's uptime at submit time. Never written to from Java except via the
 * admin-CRUD proxy calling through to Form Studio itself.
 */
@Entity
@Table(name = "processes")
@Getter
@Setter
public class QSProcess {

    @Id
    private Integer id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** draft / published / closed */
    private String status;

    @Column(name = "external_key")
    private String externalKey;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
