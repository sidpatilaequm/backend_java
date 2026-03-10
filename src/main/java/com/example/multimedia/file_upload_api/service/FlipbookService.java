package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.FlipbookDocument;
import com.example.multimedia.file_upload_api.entity.FlipbookHotspots;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.FlipbookDocumentRepository;
import com.example.multimedia.file_upload_api.repository.FlipbookHotspotsRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;

import java.util.Map;
import java.util.HashMap;

@Service
public class FlipbookService {

    @Autowired
    private FlipbookDocumentRepository flipbookDocumentRepository;

    @Autowired
    private FlipbookHotspotsRepository flipbookHotspotsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public ServiceResponse uploadPdf(MultipartFile file, String name, Long size, Long lastModified, String sha256, String customDocKey) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (file == null || file.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("File is required");
                return response;
            }

            String filename = (name != null && !name.isBlank()) ? name : file.getOriginalFilename();
            Long fileSize = (size != null) ? size : file.getSize();
            Long modified = (lastModified != null) ? lastModified : System.currentTimeMillis();

            String docKey = (customDocKey != null && !customDocKey.isBlank()) 
                    ? customDocKey
                    : (sha256 != null && !sha256.isBlank())
                        ? "sha256:" + sha256
                        : "file:" + filename + ":" + fileSize + ":" + modified;

            int pages = determinePageCount(file);

            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();

            // Save file to local storage
            String filePath = savePdfFile(file, docKey);

            // Persist or update document record (idempotent by docKey + superAdmin)
            Optional<FlipbookDocument> existing = flipbookDocumentRepository.findByDocKeyAndSuperAdmin_SuperAdminId(docKey, superAdmin.getSuperAdminId());
            FlipbookDocument document = existing.orElseGet(FlipbookDocument::new);
            document.setDocKey(docKey);
            document.setStorageUrl(filePath);
            document.setFileSize(fileSize);
            document.setPages(pages);
            document.setSuperAdmin(superAdmin);
            flipbookDocumentRepository.save(document);

            response.setStatus("SUCCESS");
            response.addData("docKey", docKey);
            response.addData("storageUrl", filePath);
            response.addData("fileSize", fileSize);
            response.addData("pages", pages);
            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Upload failed: " + e.getMessage());
            return response;
        }
    }

    public ServiceResponse saveHotspots(String docKey, Integer totalPages, String hotspotsJson) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (docKey == null || docKey.isBlank()) {
                response.setStatus("ERROR");
                response.setStatusMsg("docKey is required");
                return response;
            }
            if (hotspotsJson == null || hotspotsJson.isBlank()) {
                response.setStatus("ERROR");
                response.setStatusMsg("hotspots payload is required");
                return response;
            }

            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();

            // Try to find existing hotspot first
            Optional<FlipbookHotspots> existingOpt = flipbookHotspotsRepository
                    .findFirstByDocKeyAndSuperAdmin_SuperAdminIdOrderByVersionDesc(docKey, superAdmin.getSuperAdminId());

            int effectiveVersion;
            if (existingOpt.isPresent()) {
                // Update existing hotspot
                FlipbookHotspots existing = existingOpt.get();
                existing.setTotalPages(totalPages);
                existing.setHotspotsJson(hotspotsJson);
                flipbookHotspotsRepository.save(existing);
                effectiveVersion = existing.getVersion();
            } else {
                // Try to insert new hotspot with retry logic for concurrency
                effectiveVersion = insertNewHotspotWithRetry(docKey, totalPages, hotspotsJson, superAdmin);
            }

            response.setStatus("SUCCESS");
            response.addData("docKey", docKey);
            response.addData("totalPages", totalPages);
            response.addData("version", effectiveVersion);
            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Save hotspots failed: " + e.getMessage());
            return response;
        }
    }

    /**
     * Insert new hotspot with retry logic to handle concurrent insertions
     */
    private int insertNewHotspotWithRetry(String docKey, Integer totalPages, String hotspotsJson, SuperAdmin superAdmin) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // Check again if hotspot was created by another thread
                Optional<FlipbookHotspots> existingOpt = flipbookHotspotsRepository
                        .findFirstByDocKeyAndSuperAdmin_SuperAdminIdOrderByVersionDesc(docKey, superAdmin.getSuperAdminId());
                
                if (existingOpt.isPresent()) {
                    // Another thread created it, update it instead
                    FlipbookHotspots existing = existingOpt.get();
                    existing.setTotalPages(totalPages);
                    existing.setHotspotsJson(hotspotsJson);
                    flipbookHotspotsRepository.save(existing);
                    return existing.getVersion();
                }
                
                // Try to insert new hotspot
                FlipbookHotspots hotspots = new FlipbookHotspots();
                hotspots.setDocKey(docKey);
                hotspots.setTotalPages(totalPages);
                hotspots.setHotspotsJson(hotspotsJson);
                hotspots.setVersion(1);
                hotspots.setSuperAdmin(superAdmin);
                flipbookHotspotsRepository.save(hotspots);
                return 1;
                
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Handle duplicate key constraint violation
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Failed to save hotspots after " + maxRetries + " attempts due to concurrent access", e);
                }
                
                // Wait briefly before retry
                try {
                    Thread.sleep(100 * retryCount); // Progressive backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during hotspot save retry", ie);
                }
            }
        }
        
        throw new RuntimeException("Failed to save hotspots after maximum retries");
    }

    public ServiceResponse loadHotspots(String docKey) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (docKey == null || docKey.isBlank()) {
                response.setStatus("ERROR");
                response.setStatusMsg("docKey is required");
                return response;
            }

            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();

            Optional<FlipbookHotspots> latest = flipbookHotspotsRepository
                    .findFirstByDocKeyAndSuperAdmin_SuperAdminIdOrderByVersionDesc(docKey, superAdmin.getSuperAdminId());
            if (latest.isEmpty()) {
                response.setStatus("SUCCESS");
                response.addData("docKey", docKey);
                response.addData("totalPages", 0);
                response.addData("hotspotsJson", "{}");
                return response;
            }

            FlipbookHotspots hotspots = latest.get();
            response.setStatus("SUCCESS");
            response.addData("docKey", hotspots.getDocKey());
            response.addData("totalPages", hotspots.getTotalPages());
            response.addData("hotspotsJson", hotspots.getHotspotsJson());
            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Load hotspots failed: " + e.getMessage());
            return response;
        }
    }

    public ServiceResponse savePdf(MultipartFile file, String docKey, String name, Long size, Long lastModified, String sha256, Boolean overwrite) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (file == null || file.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("File is required");
                return response;
            }
            if (docKey == null || docKey.isBlank()) {
                response.setStatus("ERROR");
                response.setStatusMsg("docKey is required");
                return response;
            }

            Long fileSize = (size != null) ? size : file.getSize();

            int pages = determinePageCount(file);
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();

            // Save file to local storage
            String filePath = savePdfFile(file, docKey);

            // Check if document already exists
            Optional<FlipbookDocument> existing = flipbookDocumentRepository.findByDocKeyAndSuperAdmin_SuperAdminId(docKey, superAdmin.getSuperAdminId());
            
            if (existing.isPresent() && !overwrite) {
                response.setStatus("ERROR");
                response.setStatusMsg("Document with this docKey already exists. Use overwrite=true to replace it.");
                response.addData("docKey", docKey);
                response.addData("existingDocument", true);
                return response;
            }
            
            // Create or update document record
            FlipbookDocument document = existing.orElseGet(FlipbookDocument::new);
            document.setDocKey(docKey);
            document.setStorageUrl(filePath);
            document.setFileSize(fileSize);
            document.setPages(pages);
            document.setSuperAdmin(superAdmin);
            flipbookDocumentRepository.save(document);

            response.setStatus("SUCCESS");
            response.setStatusMsg("PDF saved successfully");
            response.addData("docKey", docKey);
            response.addData("filePath", filePath);
            response.addData("overwritten", existing.isPresent());
            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Save PDF failed: " + e.getMessage());
            return response;
        }
    }

    public ServiceResponse savePdfWithHotspots(MultipartFile file, String docKey, String name, Long size, Long lastModified, String sha256, Boolean overwrite, String hotspotsJson) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (file == null || file.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("File is required");
                return response;
            }
            if (docKey == null || docKey.isBlank()) {
                response.setStatus("ERROR");
                response.setStatusMsg("docKey is required");
                return response;
            }

            Long fileSize = (size != null) ? size : file.getSize();
            int pages = determinePageCount(file);
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();

            // Save file to local storage
            String filePath = savePdfFile(file, docKey);

            // Check if document already exists
            Optional<FlipbookDocument> existing = flipbookDocumentRepository.findByDocKeyAndSuperAdmin_SuperAdminId(docKey, superAdmin.getSuperAdminId());
            
            if (existing.isPresent() && !overwrite) {
                response.setStatus("ERROR");
                response.setStatusMsg("Document with this docKey already exists. Use overwrite=true to replace it.");
                response.addData("docKey", docKey);
                response.addData("existingDocument", true);
                return response;
            }
            
            // Create or update document record
            FlipbookDocument document = existing.orElseGet(FlipbookDocument::new);
            document.setDocKey(docKey);
            document.setStorageUrl(filePath);
            document.setFileSize(fileSize);
            document.setPages(pages);
            document.setSuperAdmin(superAdmin);
            flipbookDocumentRepository.save(document);

            // Handle hotspots if provided
            if (hotspotsJson != null && !hotspotsJson.isBlank()) {
                // Extract totalPages from hotspots JSON if not provided
                Integer totalPages = pages;
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(hotspotsJson);
                    if (node.has("totalPages")) {
                        totalPages = node.get("totalPages").asInt();
                    }
                } catch (Exception ignored) {}

                // Save hotspots
                ServiceResponse hotspotsResponse = saveHotspots(docKey, totalPages, hotspotsJson);
                if (!hotspotsResponse.getStatus().equals("SUCCESS")) {
                    response.setStatus("WARNING");
                    response.setStatusMsg("PDF saved but hotspots failed: " + hotspotsResponse.getStatusMsg());
                    response.addData("docKey", docKey);
                    response.addData("filePath", filePath);
                    response.addData("overwritten", existing.isPresent());
                    return response;
                }
            }

            response.setStatus("SUCCESS");
            response.setStatusMsg("PDF and hotspots saved successfully");
            response.addData("docKey", docKey);
            response.addData("filePath", filePath);
            response.addData("overwritten", existing.isPresent());
            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Save PDF with hotspots failed: " + e.getMessage());
            return response;
        }
    }

    public ServiceResponse clearHotspots(String docKey) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (docKey == null || docKey.isBlank()) {
                response.setStatus("ERROR");
                response.setStatusMsg("docKey is required");
                return response;
            }

            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();

            // Find and delete all hotspots for this document
            List<FlipbookHotspots> hotspots = flipbookHotspotsRepository.findByDocKeyAndSuperAdmin_SuperAdminId(docKey, superAdmin.getSuperAdminId());
            if (!hotspots.isEmpty()) {
                flipbookHotspotsRepository.deleteAll(hotspots);
                response.setStatus("SUCCESS");
                response.setStatusMsg("Hotspots cleared successfully");
                response.addData("docKey", docKey);
                response.addData("deletedCount", hotspots.size());
            } else {
                response.setStatus("SUCCESS");
                response.setStatusMsg("No hotspots found to clear");
                response.addData("docKey", docKey);
            }

            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Clear hotspots failed: " + e.getMessage());
            return response;
        }
    }

    public byte[] loadPdf(String docKey) {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            Optional<FlipbookDocument> document = flipbookDocumentRepository.findByDocKeyAndSuperAdmin_SuperAdminId(docKey, superAdmin.getSuperAdminId());
            
            if (document.isEmpty()) {
                throw new RuntimeException("PDF not found");
            }

            String filePath = document.get().getStorageUrl();
            if (filePath == null || filePath.isBlank()) {
                throw new RuntimeException("PDF file path not found");
            }

            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("PDF file not found on disk");
            }

            return Files.readAllBytes(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PDF: " + e.getMessage());
        }
    }

    public ServiceResponse deletePdf(String docKey) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (docKey == null || docKey.isBlank()) {
                response.setStatus("ERROR");
                response.setStatusMsg("docKey is required");
                return response;
            }

            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();

            // Delete hotspots first
            List<FlipbookHotspots> hotspots = flipbookHotspotsRepository.findAll().stream()
                    .filter(h -> h.getDocKey().equals(docKey) && h.getSuperAdmin().getSuperAdminId().equals(superAdmin.getSuperAdminId()))
                    .collect(Collectors.toList());
            flipbookHotspotsRepository.deleteAll(hotspots);

            // Delete document and file
            Optional<FlipbookDocument> document = flipbookDocumentRepository.findByDocKeyAndSuperAdmin_SuperAdminId(docKey, superAdmin.getSuperAdminId());
            if (document.isPresent()) {
                String filePath = document.get().getStorageUrl();
                if (filePath != null && !filePath.isBlank()) {
                    try {
                        Files.deleteIfExists(Paths.get(filePath));
                    } catch (Exception e) {
                        // Log but don't fail the operation
                    }
                }
                flipbookDocumentRepository.delete(document.get());
            }

            response.setStatus("SUCCESS");
            response.setStatusMsg("PDF and hotspots deleted successfully");
            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Delete failed: " + e.getMessage());
            return response;
        }
    }

    private String savePdfFile(MultipartFile file, String docKey) throws IOException {
        // Create uploads directory if it doesn't exist
        Path uploadsDir = Paths.get("uploads", "pdfs");
        Files.createDirectories(uploadsDir);

        // Generate safe filename from docKey
        String safeFilename = docKey.replaceAll("[^a-zA-Z0-9._-]", "_") + ".pdf";
        Path filePath = uploadsDir.resolve(safeFilename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public byte[] generatePdfWithHotspots(String docKey) {
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Get the original PDF
            Optional<FlipbookDocument> document = flipbookDocumentRepository.findByDocKeyAndSuperAdmin_SuperAdminId(docKey, superAdmin.getSuperAdminId());
            if (document.isEmpty()) {
                throw new RuntimeException("PDF not found");
            }
            
            String filePath = document.get().getStorageUrl();
            if (filePath == null || filePath.isBlank()) {
                throw new RuntimeException("PDF file path not found");
            }
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("PDF file not found on disk");
            }
            
            // Get hotspots data
            Optional<FlipbookHotspots> hotspots = flipbookHotspotsRepository
                    .findFirstByDocKeyAndSuperAdmin_SuperAdminIdOrderByVersionDesc(docKey, superAdmin.getSuperAdminId());
            
            if (hotspots.isEmpty()) {
                // Return original PDF if no hotspots
                return Files.readAllBytes(path);
            }
            
            // Load original PDF
            PDDocument originalDoc = PDDocument.load(Files.readAllBytes(path));
            
            // Parse hotspots JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode hotspotsJson = mapper.readTree(hotspots.get().getHotspotsJson());
            
            // Add hotspots to PDF
            addHotspotsToPdf(originalDoc, hotspotsJson);
            
            // Save modified PDF to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            originalDoc.save(outputStream);
            originalDoc.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF with hotspots: " + e.getMessage());
        }
    }
    
    private void addHotspotsToPdf(PDDocument document, JsonNode hotspotsJson) throws IOException {
        JsonNode hotspots = hotspotsJson.get("hotspots");
        if (hotspots == null) return;
        
        int pageCount = document.getNumberOfPages();
        
        // Iterate through each page
        for (int pageNum = 1; pageNum <= pageCount; pageNum++) {
            String pageKey = String.valueOf(pageNum);
            JsonNode pageHotspots = hotspots.get(pageKey);
            
            if (pageHotspots == null || !pageHotspots.isArray()) continue;
            
            PDPage page = document.getPage(pageNum - 1); // PDFBox uses 0-based indexing
            PDRectangle pageSize = page.getMediaBox();
            
            // Add each hotspot as a clickable link
            for (JsonNode hotspot : pageHotspots) {
                addHotspotToPage(page, hotspot, pageSize);
            }
        }
    }
    
    private void addHotspotToPage(PDPage page, JsonNode hotspot, PDRectangle pageSize) throws IOException {
        try {
            // Extract hotspot coordinates (normalized 0-1)
            double x = hotspot.get("x").asDouble();
            double y = hotspot.get("y").asDouble();
            double w = hotspot.get("w").asDouble();
            double h = hotspot.get("h").asDouble();
            String href = hotspot.get("href").asText();
            
            // Convert normalized coordinates to PDF coordinates
            // PDF coordinates start from bottom-left, but frontend coordinates start from top-left
            float left = (float) (x * pageSize.getWidth());
            float bottom = (float) ((1.0 - y - h) * pageSize.getHeight()); // Flip Y coordinate
            float width = (float) (w * pageSize.getWidth());
            float height = (float) (h * pageSize.getHeight()); 
            
            // Ensure coordinates are within page bounds
            left = Math.max(0, Math.min(left, pageSize.getWidth()));
            bottom = Math.max(0, Math.min(bottom, pageSize.getHeight()));
            width = Math.max(1, Math.min(width, pageSize.getWidth() - left));
            height = Math.max(1, Math.min(height, pageSize.getHeight() - bottom));
            
            // Create link annotation
            PDAnnotationLink link = new PDAnnotationLink();
            link.setRectangle(new PDRectangle(left, bottom, width, height));
            
            // Create URI action
            PDActionURI action = new PDActionURI();
            action.setURI(href);
            link.setAction(action);
            
            // Set annotation properties for better visibility
            // Note: Basic link annotations don't support custom colors in PDFBox
            // The hotspot will be clickable but may not be visually highlighted
            
            // Add annotation to page
            page.getAnnotations().add(link);
            
        } catch (Exception e) {
            // Log error but continue with other hotspots
            System.err.println("Error adding hotspot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int determinePageCount(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream(); PDDocument doc = PDDocument.load(is)) {
            return doc.getNumberOfPages();
        }
    }

    public ServiceResponse getAllPdfsForCurrentSuperAdmin() {
        ServiceResponse response = new ServiceResponse();
        try {
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            List<FlipbookDocument> documents = flipbookDocumentRepository.findBySuperAdmin_SuperAdminId(superAdmin.getSuperAdminId());
            
            List<Map<String, Object>> pdfList = documents.stream().map(doc -> {
                Map<String, Object> pdfInfo = new HashMap<>();
                pdfInfo.put("docKey", doc.getDocKey());
                pdfInfo.put("fileSize", doc.getFileSize());
                pdfInfo.put("pages", doc.getPages());
                pdfInfo.put("createdAt", doc.getCreatedAt());
                pdfInfo.put("updatedAt", doc.getUpdatedAt());
                return pdfInfo;
            }).collect(Collectors.toList());
            
            response.setStatus("SUCCESS");
            response.addData("pdfs", pdfList);
            response.addData("totalCount", pdfList.size());
            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to get PDFs: " + e.getMessage());
            return response;
        }
    }

    public ServiceResponse getPdfDetails(String docKey) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (docKey == null || docKey.isBlank()) {
                response.setStatus("ERROR");
                response.setStatusMsg("docKey is required");
                return response;
            }

            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            Optional<FlipbookDocument> document = flipbookDocumentRepository.findByDocKeyAndSuperAdmin_SuperAdminId(docKey, superAdmin.getSuperAdminId());
            
            if (document.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("PDF not found");
                return response;
            }

            FlipbookDocument doc = document.get();
            Map<String, Object> pdfInfo = new HashMap<>();
            pdfInfo.put("docKey", doc.getDocKey());
            pdfInfo.put("fileSize", doc.getFileSize());
            pdfInfo.put("pages", doc.getPages());
            pdfInfo.put("storageUrl", doc.getStorageUrl());
            pdfInfo.put("createdAt", doc.getCreatedAt());
            pdfInfo.put("updatedAt", doc.getUpdatedAt());

            // Check if hotspots exist
            Optional<FlipbookHotspots> hotspots = flipbookHotspotsRepository
                    .findFirstByDocKeyAndSuperAdmin_SuperAdminIdOrderByVersionDesc(docKey, superAdmin.getSuperAdminId());
            
            if (hotspots.isPresent()) {
                pdfInfo.put("hasHotspots", true);
                pdfInfo.put("hotspotVersion", hotspots.get().getVersion());
                pdfInfo.put("hotspotCreatedAt", hotspots.get().getCreatedAt());
            } else {
                pdfInfo.put("hasHotspots", false);
            }

            response.setStatus("SUCCESS");
            response.addData("pdf", pdfInfo);
            return response;
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to get PDF details: " + e.getMessage());
            return response;
        }
    }
}


