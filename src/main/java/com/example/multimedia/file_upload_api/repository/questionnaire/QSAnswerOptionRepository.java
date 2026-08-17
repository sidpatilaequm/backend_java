package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSAnswerOption;
import com.example.multimedia.file_upload_api.entity.questionnaire.QSAnswerOptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QSAnswerOptionRepository extends JpaRepository<QSAnswerOption, QSAnswerOptionId> {
    List<QSAnswerOption> findByAnswerIdIn(List<Integer> answerIds);
}
