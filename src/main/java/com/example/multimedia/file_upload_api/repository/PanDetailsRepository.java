package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanDetailsRepository extends JpaRepository<PanDetails, Long> {
    PanDetails findByPanNumber(String panNumber);
}