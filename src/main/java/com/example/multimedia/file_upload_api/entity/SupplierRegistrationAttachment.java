package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A free-form extra file attached to a {@link SupplierRegistration} — unlike
 * {@link SupplierRegistrationDocument}, there's no fixed docType slot (an application can carry
 * any number of these), and no OCR/verification runs against them; they're just stored via
 * FolderIt and shown to the reviewer alongside the fixed documents.
 */
@Entity
@Table(name = "supplier_registration_attachment")
@Getter
@Setter
public class SupplierRegistrationAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "registration_id")
    private SupplierRegistration registration;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "folderit_file_uid")
    private String folderItFileUid;

    /** Set only when this attachment IS the answer to a dynamic file_upload question (Form
     *  Studio's questions.id) — null for a genuinely free-form extra attachment. Reusing this
     *  table rather than adding a new one gets the existing preview/download endpoint and
     *  FolderIt storage flow for free. */
    @Column(name = "question_id")
    private Integer questionId;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
}
