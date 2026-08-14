package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.Department;
import com.example.multimedia.file_upload_api.entity.Project;
import com.example.multimedia.file_upload_api.repository.DepartmentRepository;
import com.example.multimedia.file_upload_api.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project createProject(Map<String, Object> payload) {
        Project p = new Project();
        p.setName((String) payload.get("name"));
        p.setWbs((String) payload.get("wbs"));
        
        String deptCode = (String) payload.get("dept_code");
        if (deptCode != null) {
            Department d = departmentRepository.findById(deptCode).orElse(null);
            p.setDepartment(d);
        }
        return projectRepository.save(p);
    }
}
