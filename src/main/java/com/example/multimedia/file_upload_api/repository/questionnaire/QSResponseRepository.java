package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QSResponseRepository extends JpaRepository<QSResponse, Integer> {
}
