package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.SubActivity;
import com.example.multimedia.file_upload_api.service.SubActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget/sub-activities")
public class SubActivityController {

    @Autowired
    private SubActivityService subActivityService;

    @GetMapping
    public ResponseEntity<List<SubActivity>> getAllSubActivities() {
        return ResponseEntity.ok(subActivityService.getAllSubActivities());
    }

    @PostMapping
    public ResponseEntity<SubActivity> createSubActivity(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(subActivityService.createSubActivity(payload));
    }
}
