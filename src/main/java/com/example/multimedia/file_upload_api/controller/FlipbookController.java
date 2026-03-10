package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.FlipbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/flipbook")
public class FlipbookController {

    @Autowired
    private FlipbookService flipbookService;

    @PostMapping(value = "/pdf/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "size", required = false) Long size,
            @RequestParam(value = "lastModified", required = false) Long lastModified,
            @RequestParam(value = "sha256", required = false) String sha256,
            @RequestParam(value = "docKey", required = false) String docKey) {
        ServiceResponse response = flipbookService.uploadPdf(file, name, size, lastModified, sha256, docKey);
        return ResponseEntity.ok(response);
    }

    public static class SaveHotspotsRequest {
        public String docKey;
        public Integer totalPages;
        public Object hotspots; // We'll store raw JSON string in service
    }

    @PostMapping(value = "/hotspots/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceResponse> saveHotspots(@RequestBody String rawJson) {
        // Pass through raw JSON so service can store as-is under hotspotsJson
        // Extract minimal fields docKey and totalPages using a lightweight approach
        String docKey = null;
        Integer totalPages = null;
        try {
            // Very light extraction without introducing new JSON libs (Jackson auto binds if we had DTO)
            // Here we rely on front-end sending valid keys; alternatively define a DTO.
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(rawJson);
            if (node.has("docKey")) docKey = node.get("docKey").asText();
            if (node.has("totalPages")) totalPages = node.get("totalPages").asInt();
        } catch (Exception ignored) {}

        ServiceResponse response = flipbookService.saveHotspots(docKey, totalPages, rawJson);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hotspots")
    public ResponseEntity<ServiceResponse> getHotspots(@RequestParam("docKey") String docKey) {
        ServiceResponse response = flipbookService.loadHotspots(docKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/pdf/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> savePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docKey", required = false) String docKey,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "size", required = false) Long size,
            @RequestParam(value = "lastModified", required = false) Long lastModified,
            @RequestParam(value = "sha256", required = false) String sha256,
            @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite) {
        
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setStatus("ERROR");
                errorResponse.setStatusMsg("File is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setStatus("ERROR");
                errorResponse.setStatusMsg("Only PDF files are allowed");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate file size (max 100MB)
            if (file.getSize() > 100 * 1024 * 1024) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setStatus("ERROR");
                errorResponse.setStatusMsg("File size exceeds 100MB limit");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Generate docKey if not provided
            String finalDocKey = docKey;
            if (finalDocKey == null || finalDocKey.trim().isEmpty()) {
                String filename = (name != null && !name.isBlank()) ? name : file.getOriginalFilename();
                Long fileSize = (size != null) ? size : file.getSize();
                Long modified = (lastModified != null) ? lastModified : System.currentTimeMillis();
                
                finalDocKey = (sha256 != null && !sha256.isBlank())
                    ? "sha256:" + sha256
                    : "file:" + filename + ":" + fileSize + ":" + modified;
            }
            
            ServiceResponse response = flipbookService.savePdf(file, finalDocKey, name, size, lastModified, sha256, overwrite);
            
            if (response.getStatus().equals("SUCCESS")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Save PDF failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping(value = "/pdf/save-with-hotspots", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> savePdfWithHotspots(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docKey", required = false) String docKey,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "size", required = false) Long size,
            @RequestParam(value = "lastModified", required = false) Long lastModified,
            @RequestParam(value = "sha256", required = false) String sha256,
            @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
            @RequestParam(value = "hotspots", required = false) String hotspotsJson) {
        
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setStatus("ERROR");
                errorResponse.setStatusMsg("File is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setStatus("ERROR");
                errorResponse.setStatusMsg("Only PDF files are allowed");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate file size (max 100MB)
            if (file.getSize() > 100 * 1024 * 1024) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setStatus("ERROR");
                errorResponse.setStatusMsg("File size exceeds 100MB limit");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Generate docKey if not provided
            String finalDocKey = docKey;
            if (finalDocKey == null || finalDocKey.trim().isEmpty()) {
                String filename = (name != null && !name.isBlank()) ? name : file.getOriginalFilename();
                Long fileSize = (size != null) ? size : file.getSize();
                Long modified = (lastModified != null) ? lastModified : System.currentTimeMillis();
                
                finalDocKey = (sha256 != null && !sha256.isBlank())
                    ? "sha256:" + sha256
                    : "file:" + filename + ":" + fileSize + ":" + modified;
            }
            
            ServiceResponse response = flipbookService.savePdfWithHotspots(file, finalDocKey, name, size, lastModified, sha256, overwrite, hotspotsJson);
            
            if (response.getStatus().equals("SUCCESS") || response.getStatus().equals("WARNING")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Save PDF with hotspots failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/hotspots/clear")
    public ResponseEntity<ServiceResponse> clearHotspots(@RequestParam("docKey") String docKey) {
        try {
            ServiceResponse response = flipbookService.clearHotspots(docKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Clear hotspots failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/pdf/load")
    public ResponseEntity<byte[]> loadPdf(@RequestParam("docKey") String docKey) {
        try {
            byte[] pdfBytes = flipbookService.loadPdf(docKey);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "document.pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pdf/download-with-hotspots")
    public ResponseEntity<byte[]> downloadPdfWithHotspots(@RequestParam("docKey") String docKey) {
        try {
            byte[] pdfBytes = flipbookService.generatePdfWithHotspots(docKey);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "document_with_hotspots.pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/pdf/delete")
    public ResponseEntity<ServiceResponse> deletePdf(@RequestBody DeletePdfRequest request) {
        ServiceResponse response = flipbookService.deletePdf(request.docKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pdfs")
    public ResponseEntity<ServiceResponse> getAllPdfs() {
        ServiceResponse response = flipbookService.getAllPdfsForCurrentSuperAdmin();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pdfs/{docKey}")
    public ResponseEntity<ServiceResponse> getPdfDetails(@PathVariable String docKey) {
        ServiceResponse response = flipbookService.getPdfDetails(docKey);
        return ResponseEntity.ok(response);
    }

    public static class DeletePdfRequest {
        public String docKey;
    }
}


