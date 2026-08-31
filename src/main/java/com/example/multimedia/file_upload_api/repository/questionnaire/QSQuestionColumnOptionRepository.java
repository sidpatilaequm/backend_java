package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSQuestionColumnOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QSQuestionColumnOptionRepository extends JpaRepository<QSQuestionColumnOption, Integer> {
    List<QSQuestionColumnOption> findByColumnIdInOrderByPosition(List<Integer> columnIds);
    List<QSQuestionColumnOption> findByColumnIdOrderByPosition(Integer columnId);
}
