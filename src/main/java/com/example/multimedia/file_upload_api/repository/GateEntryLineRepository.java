package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.GateEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GateEntryLineRepository extends JpaRepository<GateEntryLine, Long> {
}
