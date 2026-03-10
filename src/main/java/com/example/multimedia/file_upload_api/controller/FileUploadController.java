package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.entity.FileUpload;
import com.example.multimedia.file_upload_api.service.FileUploadService;
import com.example.multimedia.file_upload_api.service.FileExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import com.example.multimedia.file_upload_api.dto.FileUploadConfirmationDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private FileExtractionService fileExtractionService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;


    @PostMapping("/{documentType}")
    public ResponseEntity<FileUpload> uploadFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable String documentType,
            @RequestParam Long userId) {
        try {
            FileUpload uploadedFile = fileUploadService.uploadFile(file, documentType, userId);
            return ResponseEntity.ok(uploadedFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FileUpload>> getFilesByUserId(@PathVariable Long userId) {
        List<FileUpload> files = fileUploadService.getFilesByUserId(userId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/user/{userId}/document/{documentType}")
    public ResponseEntity<List<FileUpload>> getFilesByUserIdAndDocumentType(
            @PathVariable Long userId,
            @PathVariable String documentType) {
        List<FileUpload> files = fileUploadService.getFilesByUserIdAndDocumentType(userId, documentType);
        return ResponseEntity.ok(files);
    }

    /**
     * Process files and extract data without saving to database
     */
    @PostMapping("/extract")
    public ResponseEntity<ServiceResponse> processAndExtractData(
            @RequestParam("gstFile") MultipartFile gstFile,
            @RequestParam("panFile") MultipartFile panFile,
            @RequestParam("chequeFile") MultipartFile chequeFile,
            @RequestParam(value = "coiFile", required = false) MultipartFile coiFile,
            @RequestParam("authKey") String authKey) {
        try {
            ServiceResponse response = fileExtractionService.extractDataFromFiles(gstFile, panFile, chequeFile, coiFile, authKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            serviceControllerUtils.prepareMobileResponseErrorStatus(
                errorResponse, 
                AppConstants.ERRORCODE, 
                "Error processing files: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

}