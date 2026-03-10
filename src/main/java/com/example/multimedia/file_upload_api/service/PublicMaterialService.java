package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.PublicMaterialDetailsDTO;
import com.example.multimedia.file_upload_api.dto.RemainingProductDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.Channel;
import com.example.multimedia.file_upload_api.entity.MaterialChannelMapping;
import com.example.multimedia.file_upload_api.entity.MaterialImage;
import com.example.multimedia.file_upload_api.repository.MaterialRepository;
import com.example.multimedia.file_upload_api.repository.ChannelRepository;
import com.example.multimedia.file_upload_api.repository.MaterialChannelMappingRepository;
import com.example.multimedia.file_upload_api.repository.MaterialImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Base64;

@Service
public class PublicMaterialService {

    private static final Logger logger = LoggerFactory.getLogger(PublicMaterialService.class);

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private MaterialChannelMappingRepository materialChannelMappingRepository;

    @Autowired
    private MaterialImageRepository materialImageRepository;

    /**
     * Get material details with optional channel information (Public API)
     */
    public ServiceResponse getMaterialDetailsWithChannel(String materialId, String channelId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Validate input parameters
            if (materialId == null || materialId.trim().isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Material ID is required");
                return response;
            }

            // Parse IDs
            Long materialIdLong;
            final Long channelIdLong;
            try {
                materialIdLong = Long.parseLong(materialId);
                channelIdLong = (channelId != null && !channelId.trim().isEmpty())
                        ? Long.parseLong(channelId)
                        : null;
            } catch (NumberFormatException e) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid material ID or channel ID format");
                return response;
            }

            // Get material
            Optional<Material> materialOpt = materialRepository.findById(materialIdLong);
            if (!materialOpt.isPresent()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Material not found");
                return response;
            }

            Material material = materialOpt.get();
            Channel channel = null;
            MaterialChannelMapping mapping = null;

            if (channelIdLong != null) {
                // Validate channel only if provided
                Optional<Channel> channelOpt = channelRepository.findById(channelIdLong);
                if (!channelOpt.isPresent()) {
                    response.setStatus("ERROR");
                    response.setStatusMsg("Channel not found");
                    return response;
                }
                channel = channelOpt.get();

                // Check mapping only if channel provided
                List<MaterialChannelMapping> mappings = materialChannelMappingRepository.findAll().stream()
                    .filter(m -> m.getMaterial().getMaterialId().equals(materialIdLong) 
                        && m.getChannel().getChannelId().equals(channelIdLong)
                        && m.getStatus() != null && m.getStatus())
                    .collect(Collectors.toList());

                if (mappings.isEmpty()) {
                    response.setStatus("ERROR");
                    response.setStatusMsg("Material is not available in this channel");
                    return response;
                }
                mapping = mappings.get(0);
            }

            // Get material images
            List<MaterialImage> materialImages = materialImageRepository
                .findByMaterialOrderBySequenceOrderAsc(material);

            // Create DTO
            PublicMaterialDetailsDTO materialDetails = new PublicMaterialDetailsDTO();

            // Set material information
            materialDetails.setMaterialId(material.getMaterialId());
            materialDetails.setMaterialName(material.getMaterialName());
            materialDetails.setMaterialCode(material.getMaterialCode());
            materialDetails.setMaterialDescription(material.getDescription());
            // Price: use channel mapping price if available; else null (or derive if you have a global price)
            materialDetails.setPrice(mapping != null ? mapping.getPrice() : null);
            materialDetails.setCategory(material.getItemCategory() != null ? material.getItemCategory().getDescription() : null);
            materialDetails.setBrand(null); // Not available in Material entity
            materialDetails.setMaterial(material.getType());
            materialDetails.setFeatures(null); // Not available in Material entity
            materialDetails.setSpecifications(null); // Not available in Material entity
            materialDetails.setSize(null); // Not available in Material entity
            materialDetails.setColor(null); // Not available in Material entity
            materialDetails.setStock(mapping != null ? mapping.getStock() : null);
            materialDetails.setIsActive(!material.getBlocked());
            materialDetails.setCreatedDate(material.getCreatedDate());
            materialDetails.setModifiedDate(material.getModifiedDate());

            // Set channel information only if channel provided
            if (channel != null) {
                materialDetails.setChannelId(channel.getChannelId());
                materialDetails.setChannelName(channel.getChannelName());
                materialDetails.setChannelCode(channel.getChannelCode());
                materialDetails.setChannelDescription(channel.getDescription());
                materialDetails.setChannelIsActive(channel.getIsActive());
            }

            // Set channel-specific information from mapping
            materialDetails.setChannelSpecificDescription(null); // Not available in mapping
            materialDetails.setChannelSpecificPrice(mapping != null ? mapping.getPrice() : null);
            materialDetails.setChannelSpecificFeatures(null); // Not available in mapping

            // Set images
            List<PublicMaterialDetailsDTO.MaterialImageDTO> imageDTOs = materialImages.stream()
                .map(this::convertToImageDTO)
                .collect(Collectors.toList());
            materialDetails.setMaterialImages(imageDTOs);

            // Set barcode image if available
            if (material.getBarcodeImage() != null) {
                materialDetails.setBarcodeImage(material.getBarcodeImage());
            }

            // Set first image as Base64 if available
            if (!materialImages.isEmpty()) {
                MaterialImage firstImage = materialImages.get(0);
                if (firstImage.getImageData() != null) {
                    String base64Image = Base64.getEncoder().encodeToString(firstImage.getImageData());
                    materialDetails.setFirstImageBase64(base64Image);
                }
            }

            // Create product URL
            String productUrl = channelIdLong != null
                ? "http://127.0.0.1:8000/pages/products/" + materialId + "/?channelId=" + channelIdLong
                : "http://127.0.0.1:8000/pages/products/" + materialId;
            materialDetails.setProductUrl(productUrl);

            // Get remaining products for the same channel (excluding current material) only if channel provided
            List<RemainingProductDTO> remainingProducts = channelIdLong != null
                ? getRemainingProductsForChannel(channelIdLong, materialIdLong)
                : List.of();

            response.setStatus("SUCCESS");
            response.setStatusMsg("Material details retrieved successfully");
            response.addData("materialDetails", materialDetails);
            response.addData("totalImages", materialImages.size());
            response.addData("remainingProducts", remainingProducts);

            logger.info("Public material details retrieved successfully: materialId={}, channelId={}", 
                materialId, channelId);

        } catch (Exception e) {
            logger.error("Error retrieving public material details: materialId={}, channelId={}, error={}", 
                materialId, channelId, e.getMessage(), e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve material details: " + e.getMessage());
        }

        return response;
    }

    /**
     * Convert MaterialImage entity to DTO
     */
    private PublicMaterialDetailsDTO.MaterialImageDTO convertToImageDTO(MaterialImage materialImage) {
        PublicMaterialDetailsDTO.MaterialImageDTO dto = new PublicMaterialDetailsDTO.MaterialImageDTO();
        dto.setImageId(materialImage.getImageId());
        dto.setImageName(materialImage.getImageName());
        dto.setImageType(materialImage.getImageType());
        dto.setImageUrl(null); // Not available in MaterialImage entity
        
        // Convert image data to Base64
        if (materialImage.getImageData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(materialImage.getImageData());
            dto.setImageBase64(base64Image);
        }
        
        dto.setIsPrimary(materialImage.getSequenceOrder() == 1); // Assume first image is primary
        dto.setSequenceOrder(materialImage.getSequenceOrder());
        return dto;
    }

    /**
     * Get remaining products for the same channel (excluding current material)
     */
    private List<RemainingProductDTO> getRemainingProductsForChannel(Long channelId, Long excludeMaterialId) {
        try {
            // Get all material-channel mappings for this channel
            List<MaterialChannelMapping> allMappings = materialChannelMappingRepository.findAll().stream()
                .filter(mapping -> mapping.getChannel().getChannelId().equals(channelId)
                    && !mapping.getMaterial().getMaterialId().equals(excludeMaterialId)
                    && mapping.getStatus() != null && mapping.getStatus())
                .collect(Collectors.toList());

            return allMappings.stream()
                .map(mapping -> {
                    Material material = mapping.getMaterial();
                    
                    // Get material images
                    List<MaterialImage> materialImages = materialImageRepository
                        .findByMaterialOrderBySequenceOrderAsc(material);
                    
                    // Convert to RemainingProductDTO
                    RemainingProductDTO productDTO = new RemainingProductDTO();
                    productDTO.setMaterialId(material.getMaterialId());
                    productDTO.setMaterialName(material.getMaterialName());
                    productDTO.setMaterialCode(material.getMaterialCode());
                    productDTO.setMaterialDescription(material.getDescription());
                    productDTO.setPrice(mapping.getPrice());
                    productDTO.setChannelSpecificPrice(mapping.getPrice());
                    
                    // Convert images
                    List<RemainingProductDTO.MaterialImageDTO> imageDTOs = materialImages.stream()
                        .map(this::convertToRemainingProductImageDTO)
                        .collect(Collectors.toList());
                    productDTO.setMaterialImages(imageDTOs);
                    
                    return productDTO;
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error fetching remaining products for channel: channelId={}, error={}", 
                channelId, e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }

    /**
     * Convert MaterialImage entity to RemainingProductDTO.MaterialImageDTO
     */
    private RemainingProductDTO.MaterialImageDTO convertToRemainingProductImageDTO(MaterialImage materialImage) {
        RemainingProductDTO.MaterialImageDTO dto = new RemainingProductDTO.MaterialImageDTO();
        dto.setImageId(materialImage.getImageId());
        dto.setImageName(materialImage.getImageName());
        dto.setImageType(materialImage.getImageType());
        dto.setImageUrl(null); // Not available in MaterialImage entity
        
        // Convert image data to Base64
        if (materialImage.getImageData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(materialImage.getImageData());
            dto.setImageBase64(base64Image);
        }
        
        dto.setIsPrimary(materialImage.getSequenceOrder() == 1); // Assume first image is primary
        dto.setSequenceOrder(materialImage.getSequenceOrder());
        return dto;
    }
}
