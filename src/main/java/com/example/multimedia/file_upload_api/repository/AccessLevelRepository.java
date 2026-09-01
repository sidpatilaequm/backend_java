package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.AccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessLevelRepository extends JpaRepository<AccessLevel, AccessLevel.Pk> {
    List<AccessLevel> findByAssigneeTypeOrderBySortOrder(String assigneeType);
    List<AccessLevel> findAllByOrderByAssigneeTypeAscSortOrderAsc();
}
