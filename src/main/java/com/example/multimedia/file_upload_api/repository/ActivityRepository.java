package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Optional<Activity> findByActivityCode(String activityCode);
}
