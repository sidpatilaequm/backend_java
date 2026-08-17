package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Maps onto Form Studio's {@code answers} table — one row per answered question. */
@Entity
@Table(name = "answers")
@Getter
@Setter
public class QSAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "response_id")
    private Integer responseId;

    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "text_value", columnDefinition = "TEXT")
    private String textValue;
}
