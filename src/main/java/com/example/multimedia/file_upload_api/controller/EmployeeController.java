package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.LoginRequest;
import com.example.multimedia.file_upload_api.entity.Employee;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.EmployeeRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class EmployeeController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * POST /employee/login or /api/employee/login
     *
     * Authenticates an employee (EMPLOYEE or PURCHASE_DEPT role).
     * Returns JWT + enriched profile including department info.
     */
    @PostMapping({"/employee/login", "/api/employee/login"})
    public ResponseEntity<?> employeeLogin(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            String jwt = jwtUtil.generateToken(userDetails);

            UserDetail user = userDetailRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User details record not found for email: " + loginRequest.getEmail()));

            Optional<Employee> empOpt = employeeRepository.findByEmail(loginRequest.getEmail());
            if (!empOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "No employee record associated with this account."));
            }

            Employee employee = empOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("email", user.getEmail());
            response.put("userId", user.getUserId());
            response.put("employeeCode", employee.getEmployeeCode());
            response.put("name", employee.getName());
            response.put("title", employee.getTitle());
            response.put("deptCode", employee.getDepartment() != null ? employee.getDepartment().getDeptCode() : null);
            response.put("deptName", employee.getDepartment() != null ? employee.getDepartment().getDeptName() : null);
            response.put("managerCode", employee.getManager() != null ? employee.getManager().getEmployeeCode() : null);
            response.put("userType", user.getUserType() != null ? user.getUserType().name() : "EMPLOYEE");
            response.put("superAdminId", user.getSuperAdmin() != null ? user.getSuperAdmin().getSuperAdminId() : null);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /api/employee/profile/{userId}
     *
     * Returns employee profile for a given user_id (uses the new user_id FK on employee).
     * Useful for the frontend to fetch the logged-in employee's full profile.
     */
    @GetMapping("/api/employee/profile/{userId}")
    public ResponseEntity<?> getEmployeeProfile(@PathVariable Long userId) {
        try {
            UserDetail user = userDetailRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Use the new user_id FK for a direct, reliable lookup
            Optional<Employee> empOpt = employeeRepository.findByUserDetail_UserId(userId);
            if (!empOpt.isPresent()) {
                // Fallback: try by email (for employees not yet linked via user_id FK)
                empOpt = employeeRepository.findByEmail(user.getEmail());
            }

            if (!empOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No employee record found for this user."));
            }

            Employee employee = empOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("userType", user.getUserType() != null ? user.getUserType().name() : "EMPLOYEE");
            response.put("isActive", user.getIsActive());
            response.put("employeeCode", employee.getEmployeeCode());
            response.put("name", employee.getName());
            response.put("title", employee.getTitle());
            response.put("deptCode", employee.getDepartment() != null ? employee.getDepartment().getDeptCode() : null);
            response.put("deptName", employee.getDepartment() != null ? employee.getDepartment().getDeptName() : null);
            response.put("managerCode", employee.getManager() != null ? employee.getManager().getEmployeeCode() : null);
            response.put("superAdminId", user.getSuperAdmin() != null ? user.getSuperAdmin().getSuperAdminId() : null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

