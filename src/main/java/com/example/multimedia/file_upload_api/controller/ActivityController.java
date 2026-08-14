package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.Activity;
import com.example.multimedia.file_upload_api.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @PostMapping
    public ResponseEntity<Activity> createActivity(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(activityService.createActivity(payload));
    }
}
