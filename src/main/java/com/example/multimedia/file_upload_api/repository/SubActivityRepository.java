package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SubActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubActivityRepository extends JpaRepository<SubActivity, Long> {
    Optional<SubActivity> findBySubActivityCode(String subActivityCode);
}
