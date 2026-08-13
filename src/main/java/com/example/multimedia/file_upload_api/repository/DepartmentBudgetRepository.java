package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.DepartmentBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentBudgetRepository extends JpaRepository<DepartmentBudget, Long> {
    List<DepartmentBudget> findByBudgetVersionVersionCode(String versionCode);
    Optional<DepartmentBudget> findByBudgetVersionVersionCodeAndDepartmentCode(String versionCode, String departmentCode);
}
