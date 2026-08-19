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

    /** table-type only — a JSON array of rows, each row a {columnId (as string): cell value}
     *  object, matching Form Studio's own Answer.table_rows. Read/written as a raw JSON string
     *  (org.json), same convention this codebase already uses for other JSON-shaped columns
     *  rather than mapping it to a typed Java structure. */
    @Column(name = "table_rows", columnDefinition = "JSON")
    private String tableRowsJson;
}
