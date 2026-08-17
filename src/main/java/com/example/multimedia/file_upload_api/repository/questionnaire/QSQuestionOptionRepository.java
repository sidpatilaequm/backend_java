package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QSQuestionOptionRepository extends JpaRepository<QSQuestionOption, Integer> {
    List<QSQuestionOption> findByQuestionIdInOrderByPosition(List<Integer> questionIds);
}
