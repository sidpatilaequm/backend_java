package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Read-only mapping onto Form Studio's {@code question_column_options} table — the choice
 *  list for a {@code dropdown}-type table column, same shape as QSQuestionOption but scoped
 *  to one column instead of one question. See QSProcess. */
@Entity
@Table(name = "question_column_options")
@Getter
@Setter
public class QSQuestionColumnOption {

    @Id
    private Integer id;

    @Column(name = "column_id")
    private Integer columnId;

    private String label;

    private Integer position;
}
