package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Read-only mapping onto Form Studio's {@code question_options} table — see QSProcess. */
@Entity
@Table(name = "question_options")
@Getter
@Setter
public class QSQuestionOption {

    @Id
    private Integer id;

    @Column(name = "question_id")
    private Integer questionId;

    private String label;

    private Integer position;
}
