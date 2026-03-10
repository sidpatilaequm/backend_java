package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDetail, Long> {
} 