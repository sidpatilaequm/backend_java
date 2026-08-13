package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Department;
import com.example.multimedia.file_upload_api.enums.DepartmentType;
import com.example.multimedia.file_upload_api.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Department API — used by the frontend "Create Employee" form
 * to populate the Department dropdown.
 *
 * Base path: /api/departments
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/departments
    //   Returns all departments ordered by name — primary dropdown endpoint.
    //
    // Optional query param: ?search=fin
    //   Returns only departments whose name contains the search term (case-insensitive).
    //   Useful for a searchable dropdown / autocomplete.
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAllDepartments(
            @RequestParam(required = false) String search) {
        try {
            List<Department> departments;

            if (search != null && !search.isBlank()) {
                departments = departmentRepository.findByDeptNameContainingIgnoreCase(search.trim());
            } else {
                departments = departmentRepository.findAll();
            }

            // Sort alphabetically by display name
            departments.sort(Comparator.comparing(Department::getDeptName));

            List<Map<String, String>> response = departments.stream()
                    .map(dept -> {
                        Map<String, String> item = new LinkedHashMap<>();
                        item.put("deptCode", dept.getDeptCode());
                        item.put("deptName", dept.getDeptName());
                        // Enrich with description from the enum if the code matches
                        Arrays.stream(DepartmentType.values())
                                .filter(e -> e.getCode().equals(dept.getDeptCode()))
                                .findFirst()
                                .ifPresent(e -> item.put("description", e.getDescription()));
                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "total", response.size(),
                    "departments", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/departments/{deptCode}
    //   Returns a single department by its code.
    //   e.g. GET /api/departments/DEPT-PUR
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/{deptCode}")
    public ResponseEntity<?> getDepartmentByCode(@PathVariable String deptCode) {
        try {
            Department dept = departmentRepository.findById(deptCode)
                    .orElseThrow(() -> new RuntimeException("Department not found with code: " + deptCode));

            Map<String, String> response = new LinkedHashMap<>();
            response.put("deptCode", dept.getDeptCode());
            response.put("deptName", dept.getDeptName());

            // Enrich with description from enum
            Arrays.stream(DepartmentType.values())
                    .filter(e -> e.getCode().equals(dept.getDeptCode()))
                    .findFirst()
                    .ifPresent(e -> response.put("description", e.getDescription()));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/departments/enum/list
    //   Returns the full DepartmentType enum list directly (no DB hit).
    //   Useful during initial setup before the DB is seeded, or for a static
    //   reference of all supported department codes and descriptions.
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/enum/list")
    public ResponseEntity<?> getDepartmentEnumList() {
        List<Map<String, String>> list = Arrays.stream(DepartmentType.values())
                .sorted(Comparator.comparing(DepartmentType::getDisplayName))
                .map(dept -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("deptCode", dept.getCode());
                    item.put("deptName", dept.getDisplayName());
                    item.put("description", dept.getDescription());
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "total", list.size(),
                "departments", list
        ));
    }
}
