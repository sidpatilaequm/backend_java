package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.OrgConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgConfigRepository extends JpaRepository<OrgConfig, Long> {
}
