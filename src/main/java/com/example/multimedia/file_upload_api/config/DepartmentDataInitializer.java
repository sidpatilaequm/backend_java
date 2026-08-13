package com.example.multimedia.file_upload_api.config;

import com.example.multimedia.file_upload_api.entity.Department;
import com.example.multimedia.file_upload_api.enums.DepartmentType;
import com.example.multimedia.file_upload_api.repository.DepartmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds all standard departments into the `department` table on application startup.
 */
@Component
@Order(2)
public class DepartmentDataInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Fetch an existing org_code to satisfy the foreign key constraint
        String validOrgCode = "ORG001";
        try {
            List<String> orgCodes = jdbcTemplate.queryForList("SELECT org_code FROM organisation LIMIT 1", String.class);
            if (!orgCodes.isEmpty()) {
                validOrgCode = orgCodes.get(0);
            } else {
                // If table is empty, insert a dummy record to satisfy the FK
                try {
                    jdbcTemplate.execute("INSERT IGNORE INTO organisation (org_code) VALUES ('ORG001')");
                } catch (Exception innerEx) {
                    System.err.println("Also failed to insert with just org_code: " + innerEx.getMessage());
                }
            }
        } catch (Exception e) {
            // Ignore if organisation table behaves differently
            System.err.println("Could not query or insert into organisation table: " + e.getMessage());
        }

        for (DepartmentType dept : DepartmentType.values()) {
            // Skip if already seeded — prevents duplicate-key errors on restart
            if (departmentRepository.existsById(dept.getCode())) {
                continue;
            }

            Department department = new Department();
            department.setDeptCode(dept.getCode());
            department.setDeptName(dept.getDisplayName());
            department.setName(dept.getDisplayName());
            department.setOrgCode(validOrgCode);
            // Append the dept code to make wbs unique
            department.setWbs("WBS-" + dept.getCode());

            departmentRepository.save(department);
        }
    }
}
