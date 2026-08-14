package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.Activity;
import com.example.multimedia.file_upload_api.entity.Employee;
import com.example.multimedia.file_upload_api.entity.Project;
import com.example.multimedia.file_upload_api.repository.ActivityRepository;
import com.example.multimedia.file_upload_api.repository.EmployeeRepository;
import com.example.multimedia.file_upload_api.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ActivityService {
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public Activity createActivity(Map<String, Object> payload) {
        Activity a = new Activity();
        a.setName((String) payload.get("name"));
        a.setWbs((String) payload.get("wbs"));
        a.setCostTypeCode((String) payload.get("cost_type_code"));
        a.setStatusCode((String) payload.get("status_code"));
        
        String projCode = (String) payload.get("project_code");
        if (projCode != null) {
            Project p = projectRepository.findByProjectCode(projCode).orElse(null);
            a.setProject(p);
        }

        String empCode = (String) payload.get("employee_code");
        if (empCode != null) {
            Employee e = employeeRepository.findById(empCode).orElse(null);
            a.setOwner(e);
        }
        
        return activityRepository.save(a);
    }
}
