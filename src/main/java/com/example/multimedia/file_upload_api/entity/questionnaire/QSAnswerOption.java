package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Maps onto Form Studio's {@code answer_options} table — which choice(s) were ticked. */
@Entity
@Table(name = "answer_options")
@IdClass(QSAnswerOptionId.class)
@Getter
@Setter
public class QSAnswerOption {

    @Id
    @Column(name = "answer_id")
    private Integer answerId;

    @Id
    @Column(name = "option_id")
    private Integer optionId;
}
