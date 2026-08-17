package com.example.multimedia.file_upload_api.repository.questionnaire;

import com.example.multimedia.file_upload_api.entity.questionnaire.QSProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QSProcessRepository extends JpaRepository<QSProcess, Integer> {
    Optional<QSProcess> findByExternalKeyAndStatus(String externalKey, String status);
}
