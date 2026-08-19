package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSQuestionColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QSQuestionColumnRepository extends JpaRepository<QSQuestionColumn, Integer> {
    List<QSQuestionColumn> findByQuestionIdInOrderByPosition(List<Integer> questionIds);
    List<QSQuestionColumn> findByQuestionIdOrderByPosition(Integer questionId);
}
