package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/export")
public class ExcelExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportController.class);

    @Autowired 
    private ExcelExportService excelExportService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> downloadCompanyData(@PathVariable Long userId) {
        try {
            // Validate userId
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }

            // Generate Excel file
            byte[] excelContent = excelExportService.generateExcelReport(userId);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "company_data.xlsx");
            headers.setContentLength(excelContent.length);

            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("Error processing request for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No data found or error processing request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while generating Excel for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating Excel file: " + e.getMessage());
        }
    }
} 