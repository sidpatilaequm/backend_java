package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.VendorCatalogue;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.VendorCatalogueRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class VendorCatalogueService {

    @Autowired
    private VendorCatalogueRepository vendorCatalogueRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    /**
     * Check if catalogue exists for a vendor
     */
    @Transactional(readOnly = true)
    public ServiceResponse checkCatalogueExistence(Long vendorId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate vendorId
            if (vendorId == null) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Vendor ID is required"
                );
            }
            
            // Get current super admin
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Check if catalogue exists for this vendor under current super admin
            Optional<VendorCatalogue> catalogue = vendorCatalogueRepository
                .findByVendorIdAndSuperAdminIdAndIsActiveTrue(vendorId, currentSuperAdmin.getSuperAdminId());
            
            if (catalogue.isPresent()) {
                VendorCatalogue cat = catalogue.get();
                
                // Create response data
                Map<String, Object> catalogueInfo = new HashMap<>();
                catalogueInfo.put("fileName", cat.getFileName());
                catalogueInfo.put("uploadDate", cat.getUploadDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
                catalogueInfo.put("fileSize", formatFileSize(cat.getFileSize()));
                catalogueInfo.put("fileType", cat.getFileType());
                catalogueInfo.put("vendorId", cat.getVendorId());
                catalogueInfo.put("catalogueId", "cat_" + cat.getCatalogueId() + "_" + 
                    cat.getUploadDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                
                Map<String, Object> data = new HashMap<>();
                data.put("catalogueExists", true);
                data.put("catalogueInfo", catalogueInfo);
                
                response.setData(data);
                return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, 
                    AppConstants.SUCCESSCODE, 
                    "Catalogue found for vendor"
                );
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("catalogueExists", false);
                data.put("catalogueInfo", null);
                
                response.setData(data);
                return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, 
                    AppConstants.SUCCESSCODE, 
                    "No catalogue found for this vendor"
                );
            }
            
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, 
                AppConstants.ERRORCODE, 
                "Error checking catalogue: " + e.getMessage()
            );
        }
    }

    /**
     * Upload catalogue file for a vendor
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse uploadCatalogue(MultipartFile catalogueFile, Long vendorId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate file
            if (catalogueFile == null || catalogueFile.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "No file provided"
                );
            }

            // Validate file type
            String originalFilename = catalogueFile.getOriginalFilename();
            String fileType = getFileType(originalFilename);
            if (!isValidFileType(fileType)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Invalid file type. Only PDF, Excel, and CSV files are allowed."
                );
            }

            // Get current super admin
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Verify vendor exists
            Optional<UserDetail> vendor = userDetailRepository.findById(vendorId);
            if (!vendor.isPresent()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Vendor not found"
                );
            }

            // Check if catalogue already exists and deactivate it
            Optional<VendorCatalogue> existingCatalogue = vendorCatalogueRepository
                .findByVendorIdAndSuperAdminIdAndIsActiveTrue(vendorId, currentSuperAdmin.getSuperAdminId());
            
            if (existingCatalogue.isPresent()) {
                VendorCatalogue existing = existingCatalogue.get();
                existing.setIsActive(false);
                vendorCatalogueRepository.save(existing);
            }

            // Create new catalogue entry
            VendorCatalogue catalogue = new VendorCatalogue();
            catalogue.setVendorId(vendorId);
            catalogue.setFileName(originalFilename);
            catalogue.setFileType(fileType);
            catalogue.setFileSize(catalogueFile.getSize());
            catalogue.setFileData(catalogueFile.getBytes());
            catalogue.setIsActive(true);
            catalogue.setUser(vendor.get());
            catalogue.setSuperAdmin(currentSuperAdmin);

            VendorCatalogue savedCatalogue = vendorCatalogueRepository.save(catalogue);

            // Create response data
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", savedCatalogue.getFileName());
            data.put("uploadDate", savedCatalogue.getUploadDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            data.put("fileSize", formatFileSize(savedCatalogue.getFileSize()));
            data.put("vendorId", savedCatalogue.getVendorId());
            data.put("catalogueId", "cat_" + savedCatalogue.getCatalogueId() + "_" + 
                savedCatalogue.getUploadDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            response.addData("catalogue", data);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response, 
                AppConstants.SUCCESSCODE, 
                "Catalogue uploaded successfully"
            );
            
        } catch (IOException e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, 
                AppConstants.ERRORCODE, 
                "Error reading file: " + e.getMessage()
            );
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, 
                AppConstants.ERRORCODE, 
                "Error uploading catalogue: " + e.getMessage()
            );
        }
    }

    /**
     * Get catalogue file for download
     */
    @Transactional(readOnly = true)
    public ServiceResponse getCatalogueFile(Long vendorId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate vendorId
            if (vendorId == null) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Vendor ID is required"
                );
            }
            
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            
            Optional<VendorCatalogue> catalogue = vendorCatalogueRepository
                .findByVendorIdAndSuperAdminIdAndIsActiveTrue(vendorId, currentSuperAdmin.getSuperAdminId());
            
            if (catalogue.isPresent()) {
                VendorCatalogue cat = catalogue.get();
                
                Map<String, Object> data = new HashMap<>();
                data.put("fileName", cat.getFileName());
                data.put("fileType", cat.getFileType());
                data.put("fileSize", cat.getFileSize());
                data.put("fileData", cat.getFileData());
                
                response.addData("catalogue", data);
                return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, 
                    AppConstants.SUCCESSCODE, 
                    "Catalogue file retrieved successfully"
                );
            } else {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "No catalogue found for this vendor"
                );
            }
            
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, 
                AppConstants.ERRORCODE, 
                "Error retrieving catalogue: " + e.getMessage()
            );
        }
    }

    /**
     * Replace catalogue file for a vendor
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse replaceCatalogue(MultipartFile catalogueFile, Long vendorId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // Validate file
            if (catalogueFile == null || catalogueFile.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "No file provided"
                );
            }

            // Validate file type
            String originalFilename = catalogueFile.getOriginalFilename();
            String fileType = getFileType(originalFilename);
            if (!isValidFileType(fileType)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Invalid file type. Only PDF, Excel, and CSV files are allowed."
                );
            }

            // Validate vendorId
            if (vendorId == null) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Vendor ID is required"
                );
            }

            // Get current super admin
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Verify vendor exists
            Optional<UserDetail> vendor = userDetailRepository.findById(vendorId);
            if (!vendor.isPresent()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "Vendor not found"
                );
            }

            // Check if catalogue exists
            Optional<VendorCatalogue> existingCatalogue = vendorCatalogueRepository
                .findByVendorIdAndSuperAdminIdAndIsActiveTrue(vendorId, currentSuperAdmin.getSuperAdminId());
            
            if (!existingCatalogue.isPresent()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, 
                    AppConstants.ERRORCODE, 
                    "No existing catalogue found to replace"
                );
            }

            // Update existing catalogue
            VendorCatalogue catalogue = existingCatalogue.get();
            catalogue.setFileName(originalFilename);
            catalogue.setFileType(fileType);
            catalogue.setFileSize(catalogueFile.getSize());
            catalogue.setFileData(catalogueFile.getBytes());

            VendorCatalogue savedCatalogue = vendorCatalogueRepository.save(catalogue);

            // Create response data
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", savedCatalogue.getFileName());
            data.put("uploadDate", savedCatalogue.getUploadDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
            data.put("fileSize", formatFileSize(savedCatalogue.getFileSize()));
            data.put("vendorId", savedCatalogue.getVendorId());
            data.put("catalogueId", "cat_" + savedCatalogue.getCatalogueId() + "_" + 
                savedCatalogue.getUploadDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            response.setData(data);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response, 
                AppConstants.SUCCESSCODE, 
                "Catalogue replaced successfully"
            );
            
        } catch (IOException e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, 
                AppConstants.ERRORCODE, 
                "Error reading file: " + e.getMessage()
            );
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, 
                AppConstants.ERRORCODE, 
                "Error replacing catalogue: " + e.getMessage()
            );
        }
    }

    /**
     * Helper method to get file type from filename
     */
    private String getFileType(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) return "";
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Helper method to validate file type
     */
    private boolean isValidFileType(String fileType) {
        return "pdf".equals(fileType) || 
               "xlsx".equals(fileType) || 
               "xls".equals(fileType) || 
               "csv".equals(fileType);
    }

    /**
     * Helper method to format file size
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f%s", size, units[unitIndex]);
    }
}
