package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {
    List<BudgetItem> findByDepartmentBudgetId(Long departmentBudgetId);
}
