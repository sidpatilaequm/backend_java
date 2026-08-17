package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Maps onto Form Studio's {@code responses} table — unlike the read-only entities above, Java
 * writes here directly at submit time (SupplierRegistrationService), inserting into the exact
 * same table Form Studio's own edit-lock check (crud.count_responses) queries — so the lock
 * fires correctly with no extra coordination needed between the two services.
 */
@Entity
@Table(name = "responses")
@Getter
@Setter
public class QSResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "process_id")
    private Integer processId;

    @Column(name = "respondent_name")
    private String respondentName;

    @Column(name = "respondent_email")
    private String respondentEmail;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
