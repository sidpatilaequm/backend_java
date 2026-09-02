package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.MaterialDTO;
// import com.example.multimedia.file_upload_api.dto.MaterialImageSequenceUpdateRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.multimedia.file_upload_api.dto.MaterialCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {
    private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);

    @Autowired
    private MaterialService materialService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveMaterial(
            @RequestParam("materialName") String materialName,
            @RequestParam("description") String description,
            @RequestParam("vendorArticleNumber") String vendorArticleNumber,
            @RequestParam("blocked") Boolean blocked,
            @RequestParam("type") String type,
            @RequestParam("baseUnitOfMeasure") String baseUnitOfMeasure,
            @RequestParam("hsnCode") String hsnCode,
            @RequestParam("sku") String sku,
            @RequestParam("purchasingCode") String purchasingCode,
            @RequestParam("variantMandatory") Boolean variantMandatory,
            @RequestParam("locationId") Long locationId,

            @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage,
            @RequestPart(value = "materialImages", required = false) List<MultipartFile> materialImages) {
        try {
            MaterialCreateRequest materialRequest = new MaterialCreateRequest();
            materialRequest.setMaterialName(materialName);
            materialRequest.setDescription(description);
            materialRequest.setVendorArticleNumber(vendorArticleNumber);
            materialRequest.setBlocked(blocked);
            materialRequest.setType(type);
            materialRequest.setBaseUnitOfMeasure(baseUnitOfMeasure);
            materialRequest.setHsnCode(hsnCode);
            materialRequest.setSku(sku);
            materialRequest.setPurchasingCode(purchasingCode);
            materialRequest.setVariantMandatory(variantMandatory);
            materialRequest.setLocationId(locationId);
            // superAdminId will be set in the service from security context

            logger.info("Created material request: {}", materialRequest);

            ServiceResponse response = materialService.saveMaterial(materialRequest, barcodeImage, materialImages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing material request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to save material: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ServiceResponse> updateMaterial(@RequestBody MaterialDTO dto) {
        ServiceResponse response = materialService.updateMaterial(dto);
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/update/{materialId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMaterialWithImagesPost(
            @PathVariable Long materialId,
            @RequestParam("materialName") String materialName,
            @RequestParam("description") String description,
            @RequestParam("vendorArticleNumber") String vendorArticleNumber,
            @RequestParam("blocked") Boolean blocked,
            @RequestParam("type") String type,
            @RequestParam("baseUnitOfMeasure") String baseUnitOfMeasure,
            @RequestParam("hsnCode") String hsnCode,
            @RequestParam("sku") String sku,
            @RequestParam("purchasingCode") String purchasingCode,
            @RequestParam("variantMandatory") Boolean variantMandatory,
            @RequestParam("locationId") Long locationId,

            @RequestParam(value = "replaceImages", required = false, defaultValue = "false") Boolean replaceImages,
            @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage,
            @RequestPart(value = "materialImages", required = false) List<MultipartFile> materialImages) {
        return updateMaterialWithImagesInternal(materialId, materialName, description, vendorArticleNumber, blocked,
                type, baseUnitOfMeasure, hsnCode, sku, purchasingCode,
                variantMandatory, locationId, replaceImages, barcodeImage, materialImages);
    }

    private ResponseEntity<?> updateMaterialWithImagesInternal(
            Long materialId,
            String materialName,
            String description,
            String vendorArticleNumber,
            Boolean blocked,
            String type,
            String baseUnitOfMeasure,
            String hsnCode,
            String sku,
            String purchasingCode,
            Boolean variantMandatory,
            Long locationId,

            Boolean replaceImages,
            MultipartFile barcodeImage,
            List<MultipartFile> materialImages) {
        try {
            MaterialCreateRequest materialRequest = new MaterialCreateRequest();
            materialRequest.setMaterialName(materialName);
            materialRequest.setDescription(description);
            materialRequest.setVendorArticleNumber(vendorArticleNumber);
            materialRequest.setBlocked(blocked);
            materialRequest.setType(type);
            materialRequest.setBaseUnitOfMeasure(baseUnitOfMeasure);
            materialRequest.setHsnCode(hsnCode);
            materialRequest.setSku(sku);
            materialRequest.setPurchasingCode(purchasingCode);
            materialRequest.setVariantMandatory(variantMandatory);
            materialRequest.setLocationId(locationId);

            logger.info("Updating material with ID: {}, request: {}, replaceImages: {}", materialId, materialRequest,
                    replaceImages);

            ServiceResponse response = materialService.updateMaterialWithImages(materialId, materialRequest,
                    replaceImages, barcodeImage, materialImages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing material update request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to update material: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllMaterials(
            @RequestParam(required = false) Long locationId) {
        ServiceResponse response = materialService.filterMaterials(locationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getMaterialById(@PathVariable Long id) {
        ServiceResponse response = materialService.getMaterialById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<ServiceResponse> getMaterialsByLocationId(@PathVariable Long locationId) {
        ServiceResponse response = materialService.getMaterialsByLocationId(locationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/names-ids")
    public ResponseEntity<ServiceResponse> getMaterialNamesAndIdsBySuperAdminId() {
        try {
            Long superAdminId = 1L; // Fixed for now since we removed CurrentUserService injection here
            ServiceResponse response = materialService.getMaterialNamesAndIdsBySuperAdminId(superAdminId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching material names and IDs: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

}