package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QSSectionRepository extends JpaRepository<QSSection, Integer> {
    List<QSSection> findByProcessIdOrderByPosition(Integer processId);
}
