package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "budget_item")
@Data
public class BudgetItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_budget_id", nullable = false)
    private DepartmentBudget departmentBudget;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
}
