package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.MaterialBomExcel;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.MaterialBomExcelRepository;
import com.example.multimedia.file_upload_api.repository.MaterialRepository;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Optional;

@Service
public class MaterialBomExcelService {
    private static final Logger logger = LoggerFactory.getLogger(MaterialBomExcelService.class);

    @Autowired
    private MaterialBomExcelRepository materialBomExcelRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Save BOM Excel file for a material
     * Validates that the material belongs to the current super admin
     */
    public ServiceResponse saveBomExcel(Long materialId, MultipartFile excelFile) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Validate file
            if (excelFile == null || excelFile.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Excel file is required");
                return response;
            }

            // Validate file extension
            String originalFilename = excelFile.getOriginalFilename();
            if (originalFilename == null || 
                (!originalFilename.toLowerCase().endsWith(".xlsx") && 
                 !originalFilename.toLowerCase().endsWith(".xls"))) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid file format. Only .xlsx and .xls files are allowed");
                return response;
            }

            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = superAdmin.getSuperAdminId();

            // Get and validate material - use repository method that includes superAdminId to avoid lazy loading
            Optional<Material> materialOpt = materialRepository.findByMaterialIdAndSuperAdmin_SuperAdminId(materialId, superAdminId);
            if (materialOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Material not found");
                return response;
            }

            Material material = materialOpt.get();

            // Save file to storage
            String filePath = saveExcelFile(excelFile, materialId, superAdminId);

            // Check if BOM Excel already exists
            Optional<MaterialBomExcel> existingOpt = materialBomExcelRepository
                    .findByMaterial_MaterialIdAndSuperAdmin_SuperAdminId(materialId, superAdminId);

            MaterialBomExcel bomExcel;
            if (existingOpt.isPresent()) {
                // Update existing record
                bomExcel = existingOpt.get();
                // Delete old file if it exists
                try {
                    Path oldFilePath = Paths.get(bomExcel.getFilePath());
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                    }
                } catch (IOException e) {
                    logger.warn("Could not delete old BOM Excel file: {}", e.getMessage());
                }
            } else {
                // Create new record
                bomExcel = new MaterialBomExcel();
                bomExcel.setMaterial(material);
                bomExcel.setSuperAdmin(superAdmin);
            }

            bomExcel.setFilePath(filePath);
            bomExcel.setFileName(originalFilename);
            materialBomExcelRepository.save(bomExcel);

            // Prepare response
            response.setStatus("SUCCESS");
            response.setStatusMsg("BOM Excel file saved successfully");
            response.addData("file_url", "/media/bom_excel/" + Paths.get(filePath).getFileName().toString());

            return response;

        } catch (Exception e) {
            logger.error("Error saving BOM Excel file: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setStatusMsg("Error saving BOM Excel file: " + e.getMessage());
            return response;
        }
    }

    /**
     * Get BOM Excel file information for a material
     * Validates that the material belongs to the current super admin
     */
    public ServiceResponse getBomExcel(Long materialId) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            Long superAdminId = superAdmin.getSuperAdminId();

            // Get and validate material - use repository method that includes superAdminId to avoid lazy loading
            // This validates that the material exists and belongs to the current super admin
            if (!materialRepository.findByMaterialIdAndSuperAdmin_SuperAdminId(materialId, superAdminId).isPresent()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Material not found");
                return response;
            }

            // Get BOM Excel
            Optional<MaterialBomExcel> bomExcelOpt = materialBomExcelRepository
                    .findByMaterial_MaterialIdAndSuperAdmin_SuperAdminId(materialId, superAdminId);

            if (bomExcelOpt.isEmpty()) {
                response.setStatus("SUCCESS");
                response.setStatusMsg("No BOM Excel file found for this material");
                response.addData("file_exists", false);
                return response;
            }

            MaterialBomExcel bomExcel = bomExcelOpt.get();
            String fileName = Paths.get(bomExcel.getFilePath()).getFileName().toString();

            // Read file content
            byte[] fileBytes = null;
            String fileDataBase64 = null;
            try {
                Path filePath = Paths.get(bomExcel.getFilePath());
                if (Files.exists(filePath)) {
                    fileBytes = Files.readAllBytes(filePath);
                    // Encode to base64 for JSON response
                    fileDataBase64 = Base64.getEncoder().encodeToString(fileBytes);
                }
            } catch (IOException e) {
                logger.warn("Could not read BOM Excel file: {}", e.getMessage());
            }

            response.setStatus("SUCCESS");
            response.setStatusMsg("BOM Excel file found");
            response.addData("file_exists", true);
            response.addData("file_url", "/media/bom_excel/" + fileName);
            response.addData("file_name", bomExcel.getFileName());
            if (fileDataBase64 != null) {
                response.addData("file_data", fileDataBase64);
                response.addData("file_size", fileBytes != null ? fileBytes.length : 0);
            }

            return response;

        } catch (Exception e) {
            logger.error("Error getting BOM Excel file: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setStatusMsg("Error getting BOM Excel file: " + e.getMessage());
            return response;
        }
    }

    /**
     * Save Excel file to storage
     */
    private String saveExcelFile(MultipartFile file, Long materialId, Long superAdminId) throws IOException {
        // Create uploads directory if it doesn't exist
        Path uploadsDir = Paths.get("uploads", "bom_excel");
        Files.createDirectories(uploadsDir);

        // Generate filename: bom_material_{materialId}_{timestamp}.{extension}
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = "bom_material_" + materialId + "_" + timestamp + extension;
        Path filePath = uploadsDir.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    /**
     * Get BOM Excel file as bytes for download
     * Validates that the material belongs to the current super admin
     */
    public byte[] getBomExcelFileBytes(Long materialId) throws IOException {
        // Get current super admin
        SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
        Long superAdminId = superAdmin.getSuperAdminId();

        // Get and validate material
        if (!materialRepository.findByMaterialIdAndSuperAdmin_SuperAdminId(materialId, superAdminId).isPresent()) {
            throw new RuntimeException("Material not found");
        }

        // Get BOM Excel
        Optional<MaterialBomExcel> bomExcelOpt = materialBomExcelRepository
                .findByMaterial_MaterialIdAndSuperAdmin_SuperAdminId(materialId, superAdminId);

        if (bomExcelOpt.isEmpty()) {
            throw new RuntimeException("BOM Excel file not found");
        }

        MaterialBomExcel bomExcel = bomExcelOpt.get();
        Path filePath = Paths.get(bomExcel.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new IOException("BOM Excel file not found on disk");
        }

        return Files.readAllBytes(filePath);
    }

    /**
     * Get BOM Excel file name for download
     */
    public String getBomExcelFileName(Long materialId) {
        // Get current super admin
        SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
        Long superAdminId = superAdmin.getSuperAdminId();

        // Get BOM Excel
        Optional<MaterialBomExcel> bomExcelOpt = materialBomExcelRepository
                .findByMaterial_MaterialIdAndSuperAdmin_SuperAdminId(materialId, superAdminId);

        if (bomExcelOpt.isEmpty()) {
            return null;
        }

        return bomExcelOpt.get().getFileName();
    }
}

