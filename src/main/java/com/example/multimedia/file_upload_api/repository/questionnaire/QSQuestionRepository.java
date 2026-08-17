package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QSQuestionRepository extends JpaRepository<QSQuestion, Integer> {
    List<QSQuestion> findBySectionIdInOrderByPosition(List<Integer> sectionIds);
}
