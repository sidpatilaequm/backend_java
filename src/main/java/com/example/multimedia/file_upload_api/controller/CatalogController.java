package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.CatalogPdfRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.CatalogPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    @Autowired
    private CatalogPdfService catalogPdfService;

    /**
     * Generate PDF catalog from frontend data
     */
    @PostMapping("/generate-pdf")
    public ResponseEntity<?> generateCatalogPdf(@RequestBody CatalogPdfRequest request) {
        try {
            logger.info("Generating catalog PDF for channel: {}", 
                request != null ? request.getChannelName() : "null");
            
            ServiceResponse response = catalogPdfService.generateCatalogPdf(request);
            
            if ("SUCCESS".equals(response.getStatus())) {
                // Get PDF bytes from response
                byte[] pdfBytes = (byte[]) response.getData().get("pdfBytes");
                String filename = (String) response.getData().get("filename");
                
                // Set headers for PDF download
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", filename);
                headers.setContentLength(pdfBytes.length);
                
                logger.info("Catalog PDF generated successfully: {} bytes", pdfBytes.length);
                
                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            } else {
                logger.error("Failed to generate catalog PDF: {}", response.getStatusMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error generating catalog PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to generate catalog PDF: " + e.getMessage());
        }
    }

    /**
     * Generate PDF catalog and return as base64 string (for frontend display)
     */
    @PostMapping("/generate-pdf-base64")
    public ResponseEntity<?> generateCatalogPdfBase64(@RequestBody CatalogPdfRequest request) {
        try {
            logger.info("Generating catalog PDF (base64) for channel: {}", 
                request != null ? request.getChannelName() : "null");
            
            ServiceResponse response = catalogPdfService.generateCatalogPdf(request);
            
            if ("SUCCESS".equals(response.getStatus())) {
                // Convert PDF bytes to base64
                byte[] pdfBytes = (byte[]) response.getData().get("pdfBytes");
                String base64Pdf = java.util.Base64.getEncoder().encodeToString(pdfBytes);
                
                // Add base64 data to response
                response.addData("pdfBase64", base64Pdf);
                
                logger.info("Catalog PDF generated successfully (base64): {} bytes", pdfBytes.length);
                return ResponseEntity.ok(response);
            } else {
                logger.error("Failed to generate catalog PDF: {}", response.getStatusMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error generating catalog PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to generate catalog PDF: " + e.getMessage());
        }
    }
}
