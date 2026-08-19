package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QSAnswerRepository extends JpaRepository<QSAnswer, Integer> {
    List<QSAnswer> findByResponseId(Integer responseId);
    Optional<QSAnswer> findByResponseIdAndQuestionId(Integer responseId, Integer questionId);
}
