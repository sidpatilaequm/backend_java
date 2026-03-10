package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.EmailRequestDTO;
import com.example.multimedia.file_upload_api.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDTO request) {
        try {
            emailService.sendSimpleEmailToUserId(request.getUserId(), request.getSubject(), request.getBody());
            return ResponseEntity.ok("Email sent successfully to user ID " + request.getUserId());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

}
