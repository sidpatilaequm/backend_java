package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyCoverPhoto;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.CompanyCoverPhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CompanyCoverPhotoService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyCoverPhotoService.class);

    @Autowired
    private CompanyCoverPhotoRepository coverPhotoRepository;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Upload cover photo for company
     */
    @Transactional
    public ServiceResponse uploadCoverPhoto(MultipartFile file) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("No file provided");
                return response;
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.setStatus("ERROR");
                response.setStatusMsg("Only image files are allowed");
                return response;
            }

            // Validate file size (max 10MB)
            long maxFileSize = 10 * 1024 * 1024; // 10MB in bytes
            if (file.getSize() > maxFileSize) {
                response.setStatus("ERROR");
                response.setStatusMsg("File size too large. Maximum allowed size is 10MB");
                return response;
            }

            // Log file information for debugging
            logger.info("Uploading cover photo: name={}, size={} bytes, type={}", 
                file.getOriginalFilename(), file.getSize(), contentType);

            // Get current user
            SuperAdmin currentUser = currentUserService.getCurrentSuperAdmin();
            if (currentUser == null) {
                response.setStatus("ERROR");
                response.setStatusMsg("User not authenticated");
                return response;
            }

            // For now, we'll use the SuperAdmin ID as company ID
            // In a real scenario, you might need to get the company from SuperAdmin
            Long companyId = currentUser.getSuperAdminId();

            // Create cover photo entity
            CompanyCoverPhoto coverPhoto = new CompanyCoverPhoto();
            coverPhoto.setCompanyId(companyId);
            coverPhoto.setCoverPhotoName(file.getOriginalFilename());
            coverPhoto.setCoverPhotoType(contentType);
            coverPhoto.setCoverPhotoData(file.getBytes());
            coverPhoto.setIsActive(true);
            coverPhoto.setCreatedBy(currentUser.getEmail());
            coverPhoto.setCreatedDate(LocalDateTime.now());

            // Check if company already has a cover photo and replace it
            List<CompanyCoverPhoto> existingPhotos = coverPhotoRepository
                .findByCompanyIdAndIsActiveTrueOrderBySequenceOrderAsc(companyId);
            
            if (!existingPhotos.isEmpty()) {
                // Deactivate existing photos (soft delete)
                for (CompanyCoverPhoto existingPhoto : existingPhotos) {
                    existingPhoto.setIsActive(false);
                    existingPhoto.setModifiedDate(LocalDateTime.now());
                    existingPhoto.setModifiedBy(currentUser.getEmail());
                    coverPhotoRepository.save(existingPhoto);
                }
            }

            // Set sequence order to 0 for the new primary photo
            coverPhoto.setSequenceOrder(0);

            // Save new cover photo
            CompanyCoverPhoto savedCoverPhoto = coverPhotoRepository.save(coverPhoto);

            response.setStatus("SUCCESS");
            response.setStatusMsg(existingPhotos.isEmpty() ? 
                "Cover photo uploaded successfully" : 
                "Cover photo uploaded and previous photo replaced successfully");
            response.addData("coverPhotoId", savedCoverPhoto.getCoverPhotoId());
            response.addData("coverPhotoName", savedCoverPhoto.getCoverPhotoName());
            response.addData("coverPhotoType", savedCoverPhoto.getCoverPhotoType());
            response.addData("sequenceOrder", savedCoverPhoto.getSequenceOrder());
            response.addData("fileSize", file.getSize());

        } catch (IOException e) {
            logger.error("IO Error uploading cover photo: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to process file: " + e.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Database constraint violation: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setStatusMsg("File too large or database error. Please try a smaller image file.");
        } catch (Exception e) {
            logger.error("Unexpected error uploading cover photo: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to upload cover photo: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get all cover photos for company
     */
    public ServiceResponse getCompanyCoverPhotos() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current user
            SuperAdmin currentUser = currentUserService.getCurrentSuperAdmin();
            if (currentUser == null) {
                response.setStatus("ERROR");
                response.setStatusMsg("User not authenticated");
                return response;
            }

            Long companyId = currentUser.getSuperAdminId();

            // Get cover photos
            List<CompanyCoverPhoto> coverPhotos = coverPhotoRepository
                .findByCompanyIdAndIsActiveTrueOrderBySequenceOrderAsc(companyId);

            response.setStatus("SUCCESS");
            response.setStatusMsg("Cover photos retrieved successfully");
            response.addData("coverPhotos", coverPhotos);
            response.addData("totalCoverPhotos", coverPhotos.size());

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve cover photos: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get primary cover photo for company
     */
    public ServiceResponse getPrimaryCoverPhoto() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current user
            SuperAdmin currentUser = currentUserService.getCurrentSuperAdmin();
            if (currentUser == null) {
                response.setStatus("ERROR");
                response.setStatusMsg("User not authenticated");
                return response;
            }

            Long companyId = currentUser.getSuperAdminId();

            // Get primary cover photo
            Optional<CompanyCoverPhoto> coverPhotoOpt = coverPhotoRepository
                .findPrimaryCoverPhotoByCompanyId(companyId);

            if (coverPhotoOpt.isPresent()) {
                CompanyCoverPhoto coverPhoto = coverPhotoOpt.get();
                response.setStatus("SUCCESS");
                response.setStatusMsg("Primary cover photo retrieved successfully");
                response.addData("coverPhoto", coverPhoto);
            } else {
                response.setStatus("SUCCESS");
                response.setStatusMsg("No cover photo found");
                response.addData("coverPhoto", null);
            }

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve primary cover photo: " + e.getMessage());
        }

        return response;
    }

}
