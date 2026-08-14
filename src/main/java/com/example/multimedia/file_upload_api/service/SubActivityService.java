package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.Activity;
import com.example.multimedia.file_upload_api.entity.Employee;
import com.example.multimedia.file_upload_api.entity.SubActivity;
import com.example.multimedia.file_upload_api.repository.ActivityRepository;
import com.example.multimedia.file_upload_api.repository.EmployeeRepository;
import com.example.multimedia.file_upload_api.repository.SubActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SubActivityService {
    @Autowired
    private SubActivityRepository subActivityRepository;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<SubActivity> getAllSubActivities() {
        return subActivityRepository.findAll();
    }

    public SubActivity createSubActivity(Map<String, Object> payload) {
        SubActivity a = new SubActivity();
        a.setName((String) payload.get("name"));
        a.setWbs((String) payload.get("wbs"));
        a.setCostTypeCode((String) payload.get("cost_type_code"));
        a.setStatusCode((String) payload.get("status_code"));
        
        if (payload.get("level") != null) {
            a.setLevel(((Number) payload.get("level")).intValue());
        }
        
        String actCode = (String) payload.get("activity_code");
        if (actCode != null) {
            Activity act = activityRepository.findByActivityCode(actCode).orElse(null);
            a.setActivity(act);
        }

        String empCode = (String) payload.get("employee_code");
        if (empCode != null) {
            Employee e = employeeRepository.findById(empCode).orElse(null);
            a.setOwner(e);
        }
        
        return subActivityRepository.save(a);
    }
}
