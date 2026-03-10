package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.VendorCatalogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/vendor/catalogue")
public class VendorCatalogueController {

    @Autowired
    private VendorCatalogueService vendorCatalogueService;

    /**
     * Check if catalogue exists for a vendor
     * GET /api/vendor/catalogue/check?vendorId=123
     */
    @GetMapping("/check")
    public ResponseEntity<ServiceResponse> checkCatalogueExistence(@RequestParam(required = false) Long vendorId) {
        ServiceResponse response = vendorCatalogueService.checkCatalogueExistence(vendorId);
        
        // Return 400 Bad Request for error responses
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Upload catalogue file for a vendor
     * POST /api/vendor/catalogue/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> uploadCatalogue(
            @RequestParam("catalogueFile") MultipartFile catalogueFile,
            @RequestParam("vendorId") Long vendorId) {
        
        ServiceResponse response = vendorCatalogueService.uploadCatalogue(catalogueFile, vendorId);
        
        // Return 400 Bad Request for error responses
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Replace catalogue file for a vendor
     * PUT /api/vendor/catalogue/replace
     */
    @PutMapping(value = "/replace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> replaceCatalogue(
            @RequestParam("catalogueFile") MultipartFile catalogueFile,
            @RequestParam("vendorId") Long vendorId) {
        
        ServiceResponse response = vendorCatalogueService.replaceCatalogue(catalogueFile, vendorId);
        
        // Return 400 Bad Request for error responses
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Download catalogue file for a vendor
     * GET /api/vendor/catalogue/download?vendorId=123
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadCatalogue(@RequestParam(required = false) Long vendorId) {
        ServiceResponse response = vendorCatalogueService.getCatalogueFile(vendorId);
        
        // Return 400 Bad Request for error responses
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        // Extract file data from response
        @SuppressWarnings("unchecked")
        Map<String, Object> catalogueData = (Map<String, Object>) response.getData().get("catalogue");
        if (catalogueData != null) {
            String fileName = (String) catalogueData.get("fileName");
            String fileType = (String) catalogueData.get("fileType");
            byte[] fileData = (byte[]) catalogueData.get("fileData");
            
            // Set appropriate content type
            MediaType contentType = getContentType(fileType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDispositionFormData("attachment", fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
        }
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Helper method to get content type based on file type
     */
    private MediaType getContentType(String fileType) {
        switch (fileType.toLowerCase()) {
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "xlsx":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "xls":
                return MediaType.parseMediaType("application/vnd.ms-excel");
            case "csv":
                return MediaType.parseMediaType("text/csv");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
