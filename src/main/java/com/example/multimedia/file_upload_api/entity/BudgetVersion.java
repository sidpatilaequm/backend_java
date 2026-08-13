package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "budget_version")
@Data
public class BudgetVersion {
    @Id
    @Column(name = "version_code", length = 20, nullable = false)
    private String versionCode;

    @Column(name = "fiscal_year")
    private String fiscalYear;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_locked")
    private boolean isLocked = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "budgetVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DepartmentBudget> departmentBudgets;
    
    @PreUpdate
    public void setLastUpdate() {  this.updatedAt = LocalDateTime.now(); }
}
