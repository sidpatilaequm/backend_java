package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Read-only mapping onto Form Studio's {@code question_columns} table — see QSProcess. */
@Entity
@Table(name = "question_columns")
@Getter
@Setter
public class QSQuestionColumn {

    @Id
    private Integer id;

    @Column(name = "question_id")
    private Integer questionId;

    private String label;

    /** text / number / date / dropdown */
    @Column(name = "column_type")
    private String columnType;

    @Column(name = "is_required")
    private Boolean isRequired;

    private Integer position;
}
