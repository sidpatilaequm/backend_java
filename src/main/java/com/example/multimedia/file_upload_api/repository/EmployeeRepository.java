package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmail(String email);
    List<Employee> findByManager_EmployeeCode(String managerCode);
    /** Find the employee linked to a specific user_details.user_id. */
    Optional<Employee> findByUserDetail_UserId(Long userId);
    /** Find all employees in a department. */
    List<Employee> findByDepartment_DeptCode(String deptCode);
}
