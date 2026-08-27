package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserDetailRepository userDetailRepository;

    // The actual authenticated SMTP identity (spring.mail.username) — was hardcoded to a
    // leftover placeholder "your_email@gmail.com" that never matched the real sender.
    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendSimpleEmailToUserId(Long userId, String subject, String body) {
        Optional<UserDetail> userOpt = userDetailRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        String toEmail = userOpt.get().getEmail();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Mail Sent Successfully to " + toEmail);
    }
}
