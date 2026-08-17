package com.example.multimedia.file_upload_api.entity.questionnaire;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

/** Composite key for {@link QSAnswerOption} — matches answer_options' (answer_id, option_id) PK. */
@EqualsAndHashCode
public class QSAnswerOptionId implements Serializable {
    private Integer answerId;
    private Integer optionId;

    public QSAnswerOptionId() {}

    public QSAnswerOptionId(Integer answerId, Integer optionId) {
        this.answerId = answerId;
        this.optionId = optionId;
    }
}
