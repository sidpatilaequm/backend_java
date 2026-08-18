package com.example.multimedia.file_upload_api.entity.questionnaire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Read-only mapping onto Form Studio's {@code questions} table — see QSProcess. */
@Entity
@Table(name = "questions")
@Getter
@Setter
public class QSQuestion {

    @Id
    private Integer id;

    @Column(name = "section_id")
    private Integer sectionId;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "help_text")
    private String helpText;

    /** short_text / single_choice / multi_choice / counter */
    @Column(name = "question_type")
    private String questionType;

    @Column(name = "is_mandatory")
    private Boolean isMandatory;

    private Integer position;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "min_selections")
    private Integer minSelections;

    @Column(name = "max_selections")
    private Integer maxSelections;

    /** single_choice only — render as a &lt;select&gt; instead of radio buttons. */
    @Column(name = "is_dropdown")
    private Boolean isDropdown;

    /** counter only. */
    @Column(name = "min_value")
    private Integer minValue;

    @Column(name = "max_value")
    private Integer maxValue;
}
