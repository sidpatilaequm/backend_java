package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Read-only mapping onto Form Studio's {@code sections} table — see QSProcess. */
@Entity
@Table(name = "sections")
@Getter
@Setter
public class QSSection {

    @Id
    private Integer id;

    @Column(name = "process_id")
    private Integer processId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer position;
}
