package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    /** Case-insensitive name search — useful for frontend search/autocomplete. */
    List<Department> findByDeptNameContainingIgnoreCase(String name);

    /** Duplicate-name guard before saving a custom department. */
    boolean existsByDeptName(String deptName);
}

