package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.MaterialDTO;
import com.example.multimedia.file_upload_api.dto.MaterialImageSequenceUpdateRequest;
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
import com.example.multimedia.file_upload_api.dto.MaterialAttributeDTO;
import com.example.multimedia.file_upload_api.dto.VariantRequest;
import com.example.multimedia.file_upload_api.dto.VariantUpdateRequest;
import com.example.multimedia.file_upload_api.entity.MaterialVariant;
import com.example.multimedia.file_upload_api.repository.MaterialVariantRepository;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.dto.VariantBulkRequest;
import com.example.multimedia.file_upload_api.dto.VariantActiveStatusRequest;
import com.example.multimedia.file_upload_api.dto.BulkMaterialCreateRequest;
import com.example.multimedia.file_upload_api.dto.BulkMaterialResponse;
import com.example.multimedia.file_upload_api.dto.BulkMaterialUploadRequest;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import com.example.multimedia.file_upload_api.service.MaterialBomExcelService;

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

    @Autowired
    private MaterialVariantRepository materialVariantRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MaterialBomExcelService materialBomExcelService;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveMaterial(
        @RequestParam("materialName") String materialName,
        @RequestParam("description") String description,
        @RequestParam("vendorArticleNumber") String vendorArticleNumber,
        @RequestParam("blocked") Boolean blocked,
        @RequestParam("type") String type,
        @RequestParam("baseUnitOfMeasure") String baseUnitOfMeasure,
        @RequestParam("itemCategoryCode") String itemCategoryCode,
        @RequestParam("subcategoryName") String subcategoryName,
        @RequestParam("hsnCode") String hsnCode,
        @RequestParam("sku") String sku,
        @RequestParam("purchasingCode") String purchasingCode,
        @RequestParam("variantMandatory") Boolean variantMandatory,
        @RequestParam("locationId") Long locationId,
        @RequestParam(value = "attributes", required = false) String attributesJson,
        @RequestParam(value = "generalAttributes", required = false) String generalAttributesJson,
        @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage,
        @RequestPart(value = "materialImages", required = false) List<MultipartFile> materialImages
    ) {
        try {
            MaterialCreateRequest materialRequest = new MaterialCreateRequest();
            materialRequest.setMaterialName(materialName);
            materialRequest.setDescription(description);
            materialRequest.setVendorArticleNumber(vendorArticleNumber);
            materialRequest.setBlocked(blocked);
            materialRequest.setType(type);
            materialRequest.setBaseUnitOfMeasure(baseUnitOfMeasure);
            materialRequest.setItemCategoryCode(itemCategoryCode);
            materialRequest.setSubcategoryName(subcategoryName);
            materialRequest.setHsnCode(hsnCode);
            materialRequest.setSku(sku);
            materialRequest.setPurchasingCode(purchasingCode);
            materialRequest.setVariantMandatory(variantMandatory);
            materialRequest.setLocationId(locationId);
            // superAdminId will be set in the service from security context

            if (attributesJson != null && !attributesJson.isEmpty()) {
                List<MaterialAttributeDTO> attributes = objectMapper.readValue(attributesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MaterialAttributeDTO.class));
                materialRequest.setAttributes(attributes);
            }

            if (generalAttributesJson != null && !generalAttributesJson.isEmpty()) {
                List<MaterialCreateRequest.GeneralAttributeRequest> generalAttributes = objectMapper.readValue(generalAttributesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MaterialCreateRequest.GeneralAttributeRequest.class));
                materialRequest.setGeneralAttributes(generalAttributes);
            }

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

    @PostMapping(value = "/bulk-save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveMaterialsBulk(@RequestBody BulkMaterialCreateRequest request) {
        try {
            logger.info("Processing bulk material creation request with {} materials", request.getMaterials().size());
            
            BulkMaterialResponse response = materialService.saveMaterialsBulk(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing bulk material request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to save materials: " + e.getMessage());
        }
    }

    @PostMapping(value = "/bulk-save-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveMaterialsBulkWithImages(
        @RequestParam("materialsJson") String materialsJson,
        @RequestPart(value = "barcodeImages", required = false) List<MultipartFile> barcodeImages,
        @RequestPart(value = "materialImages", required = false) List<MultipartFile> materialImages
    ) {
        try {
            logger.info("Processing bulk material creation with images request");
            
            // Parse the JSON string to BulkMaterialCreateRequest
            BulkMaterialCreateRequest request = objectMapper.readValue(materialsJson, BulkMaterialCreateRequest.class);
            
            BulkMaterialResponse response = materialService.saveMaterialsBulkWithImages(request, barcodeImages, materialImages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing bulk material with images request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to save materials: " + e.getMessage());
        }
    }

    @PostMapping(value = "/bulk-upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> bulkUploadMaterials(@RequestBody BulkMaterialUploadRequest request) {
        try {
            logger.info("Processing bulk material upload request with {} materials", request.getMaterials().size());
            
            ServiceResponse response = materialService.bulkUploadMaterials(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing bulk material upload: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error processing bulk material upload: " + e.getMessage());
        }
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
        @RequestParam("itemCategoryCode") String itemCategoryCode,
        @RequestParam("subcategoryName") String subcategoryName,
        @RequestParam("hsnCode") String hsnCode,
        @RequestParam("sku") String sku,
        @RequestParam("purchasingCode") String purchasingCode,
        @RequestParam("variantMandatory") Boolean variantMandatory,
        @RequestParam("locationId") Long locationId,
        @RequestParam(value = "attributes", required = false) String attributesJson,
        @RequestParam(value = "generalAttributes", required = false) String generalAttributesJson,
        @RequestParam(value = "replaceImages", required = false, defaultValue = "false") Boolean replaceImages,
        @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage,
        @RequestPart(value = "materialImages", required = false) List<MultipartFile> materialImages
    ) {
        return updateMaterialWithImagesInternal(materialId, materialName, description, vendorArticleNumber, blocked, type, baseUnitOfMeasure, itemCategoryCode, subcategoryName, hsnCode, sku, purchasingCode, variantMandatory, locationId, attributesJson, generalAttributesJson, replaceImages, barcodeImage, materialImages);
    }

    private ResponseEntity<?> updateMaterialWithImagesInternal(
        Long materialId,
        String materialName,
        String description,
        String vendorArticleNumber,
        Boolean blocked,
        String type,
        String baseUnitOfMeasure,
        String itemCategoryCode,
        String subcategoryName,
        String hsnCode,
        String sku,
        String purchasingCode,
        Boolean variantMandatory,
        Long locationId,
        String attributesJson,
        String generalAttributesJson,
        Boolean replaceImages,
        MultipartFile barcodeImage,
        List<MultipartFile> materialImages
    ) {
        try {
            MaterialCreateRequest materialRequest = new MaterialCreateRequest();
            materialRequest.setMaterialName(materialName);
            materialRequest.setDescription(description);
            materialRequest.setVendorArticleNumber(vendorArticleNumber);
            materialRequest.setBlocked(blocked);
            materialRequest.setType(type);
            materialRequest.setBaseUnitOfMeasure(baseUnitOfMeasure);
            materialRequest.setItemCategoryCode(itemCategoryCode);
            materialRequest.setSubcategoryName(subcategoryName);
            materialRequest.setHsnCode(hsnCode);
            materialRequest.setSku(sku);
            materialRequest.setPurchasingCode(purchasingCode);
            materialRequest.setVariantMandatory(variantMandatory);
            materialRequest.setLocationId(locationId);

            if (attributesJson != null && !attributesJson.isEmpty()) {
                List<MaterialAttributeDTO> attributes = objectMapper.readValue(attributesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MaterialAttributeDTO.class));
                materialRequest.setAttributes(attributes);
            }

            if (generalAttributesJson != null && !generalAttributesJson.isEmpty()) {
                List<MaterialCreateRequest.GeneralAttributeRequest> generalAttributes = objectMapper.readValue(generalAttributesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MaterialCreateRequest.GeneralAttributeRequest.class));
                materialRequest.setGeneralAttributes(generalAttributes);
            }

            logger.info("Updating material with ID: {}, request: {}, replaceImages: {}", materialId, materialRequest, replaceImages);
            
            ServiceResponse response = materialService.updateMaterialWithImages(materialId, materialRequest, replaceImages, barcodeImage, materialImages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing material update request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to update material: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllMaterials() {
        ServiceResponse response = materialService.getAllMaterials();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ServiceResponse> getMaterialsByCategoryId(@PathVariable Long categoryId) {
        ServiceResponse response = materialService.getMaterialsByCategoryId(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subcategory/{subcategoryId}")
    public ResponseEntity<ServiceResponse> getMaterialsBySubcategoryId(@PathVariable Long subcategoryId) {
        ServiceResponse response = materialService.getMaterialsBySubcategoryId(subcategoryId);
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
            // Assume superAdminId is retrieved from the security context
            Long superAdminId = currentUserService.getCurrentSuperAdminId();
            ServiceResponse response = materialService.getMaterialNamesAndIdsBySuperAdminId(superAdminId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching material names and IDs: {}", e.getMessage(), e);
            ServiceResponse response = new ServiceResponse();
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to fetch material names and IDs: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/images/sequence")
    public ResponseEntity<?> updateMaterialImageSequences(@RequestBody MaterialImageSequenceUpdateRequest request) {
        try {
            ServiceResponse response = materialService.updateMaterialImageSequences(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating material image sequences: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to update material image sequences: " + e.getMessage());
        }
    }
    
    @PostMapping("/{materialId}/variants")
    public ResponseEntity<ServiceResponse> createVariant(
            @PathVariable Long materialId,
            @RequestBody VariantRequest request) {
        ServiceResponse response = new ServiceResponse();
        try {
        materialService.createVariant(materialId, request);
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variant Created");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/variants/{variantId}/barcode")
    public ResponseEntity<ServiceResponse> updateBarcodeImage(@PathVariable Long variantId, @RequestParam("file") MultipartFile file) throws IOException {
        ServiceResponse response = new ServiceResponse();
        try {
        MaterialVariant variant = materialVariantRepository.findById(variantId)
        .orElseThrow(() -> new RuntimeException("Variant not found"));
        variant.setBarcodeImage(file.getBytes());
        materialVariantRepository.save(variant);
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Barcode image uploaded");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/variants/all")
    public ResponseEntity<ServiceResponse> getAllMaterialVariants() {
        ServiceResponse response = materialService.getAllMaterialVariants();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{materialId}/variants/bulk")
    public ResponseEntity<ServiceResponse> createVariantsBulk(
            @PathVariable Long materialId,
            @RequestBody VariantBulkRequest request) {
        ServiceResponse response = materialService.createVariantsBulk(materialId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/variants")
    public ResponseEntity<ServiceResponse> getAllVariants() {
        ServiceResponse response = materialService.getAllVariants();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{materialId}/variants")
    public ResponseEntity<ServiceResponse> getVariantsByMaterial(@PathVariable Long materialId) {
        ServiceResponse response = materialService.getVariantsByMaterial(materialId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/variants/{variantCode}")
    public ResponseEntity<ServiceResponse> getVariantByCode(@PathVariable String variantCode) {
        ServiceResponse response = materialService.getVariantByCode(variantCode);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/variants/{variantCode}")
    public ResponseEntity<ServiceResponse> deleteVariantByCode(@PathVariable String variantCode) {
        ServiceResponse response = materialService.deleteVariantByCode(variantCode);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/variants/{variantCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> updateVariantByCode(
            @PathVariable String variantCode,
            @RequestParam(value = "mrp", required = false) Double mrp,
            @RequestParam(value = "sellingPrice", required = false) Double sellingPrice,
            @RequestParam(value = "cost", required = false) Double cost,
            @RequestParam(value = "stock", required = false) Double stock,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "attributes", required = false) String attributesJson,
            @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage,
            @RequestPart(value = "variantImage", required = false) MultipartFile variantImage) {
        return updateVariantByCodeInternal(variantCode, mrp, sellingPrice, cost, stock, isActive, attributesJson, barcodeImage, variantImage);
    }

    @PostMapping(value = "/variants/{variantCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> updateVariantByCodePost(
            @PathVariable String variantCode,
            @RequestParam(value = "mrp", required = false) Double mrp,
            @RequestParam(value = "sellingPrice", required = false) Double sellingPrice,
            @RequestParam(value = "cost", required = false) Double cost,
            @RequestParam(value = "stock", required = false) Double stock,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "attributes", required = false) String attributesJson,
            @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage,
            @RequestPart(value = "variantImage", required = false) MultipartFile variantImage) {
        return updateVariantByCodeInternal(variantCode, mrp, sellingPrice, cost, stock, isActive, attributesJson, barcodeImage, variantImage);
    }

    private ResponseEntity<ServiceResponse> updateVariantByCodeInternal(
            String variantCode,
            Double mrp,
            Double sellingPrice,
            Double cost,
            Double stock,
            Boolean isActive,
            String attributesJson,
            MultipartFile barcodeImage,
            MultipartFile variantImage) {
        try {
            VariantUpdateRequest request = new VariantUpdateRequest();
            request.setMrp(mrp);
            request.setSellingPrice(sellingPrice);
            request.setCost(cost);
            request.setStock(stock);
            request.setIsActive(isActive);

            if (attributesJson != null && !attributesJson.isEmpty()) {
                List<VariantUpdateRequest.VariantAttribute> attributes = objectMapper.readValue(attributesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, VariantUpdateRequest.VariantAttribute.class));
                request.setAttributes(attributes);
            }

            ServiceResponse response = materialService.updateVariantByCodeWithImages(variantCode, request, barcodeImage, variantImage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating variant: {}", e.getMessage(), e);
            ServiceResponse response = new ServiceResponse();
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Failed to update variant: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/variants/{variantCode}/active-status")
    public ResponseEntity<ServiceResponse> updateVariantActiveStatus(
            @PathVariable String variantCode,
            @RequestBody VariantActiveStatusRequest request) {
        ServiceResponse response = materialService.updateVariantActiveStatus(variantCode, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/variants/{variantCode}/barcode-image")
    public ResponseEntity<ServiceResponse> updateVariantBarcodeImageByCode(
            @PathVariable String variantCode, 
            @RequestParam("file") MultipartFile file) throws IOException {
        ServiceResponse response = materialService.updateVariantBarcodeImageByCode(variantCode, file);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/variants/id/{variantId}/variant-image")
    public ResponseEntity<ServiceResponse> updateVariantImage(@PathVariable Long variantId, @RequestParam("file") MultipartFile file) throws IOException {
        ServiceResponse response = new ServiceResponse();
        try {
            MaterialVariant variant = materialVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
            variant.setVariantImage(file.getBytes());
            materialVariantRepository.save(variant);
            serviceControllerUtils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Variant image uploaded");
        } catch (Exception e) {
            serviceControllerUtils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/variants/code/{variantCode}/variant-image")
    public ResponseEntity<ServiceResponse> updateVariantImageByCode(
            @PathVariable String variantCode, 
            @RequestParam("file") MultipartFile file) throws IOException {
        ServiceResponse response = materialService.updateVariantImageByCode(variantCode, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{materialId}/attributes")
    public ResponseEntity<ServiceResponse> getMaterialAttributes(@PathVariable Long materialId) {
        ServiceResponse response = materialService.getMaterialAttributes(materialId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<ServiceResponse> deleteMaterial(@PathVariable Long materialId) {
        ServiceResponse response = materialService.deleteMaterial(materialId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug/categories")
    public ResponseEntity<?> debugCategories() {
        try {
            ServiceResponse response = materialService.debugCategories();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in debug categories: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Debug failed: " + e.getMessage());
        }
    }

    /**
     * Save BOM Excel file for a material
     * POST /api/materials/{materialId}/bom-excel/save
     */
    @PostMapping(value = "/{materialId}/bom-excel/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveBomExcel(
            @PathVariable Long materialId,
            @RequestPart("excel_file") MultipartFile excelFile) {
        try {
            ServiceResponse response = materialBomExcelService.saveBomExcel(materialId, excelFile);
            
            if ("ERROR".equals(response.getStatus())) {
                if (response.getStatusMsg().contains("not found")) {
                    return ResponseEntity.status(404).body(response);
                } else if (response.getStatusMsg().contains("required") || 
                           response.getStatusMsg().contains("Invalid file format")) {
                    return ResponseEntity.status(400).body(response);
                } else {
                    return ResponseEntity.status(500).body(response);
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saving BOM Excel file: {}", e.getMessage(), e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error saving BOM Excel file: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get BOM Excel file information for a material (includes file data as base64)
     * GET /api/materials/{materialId}/bom-excel/get
     */
    @GetMapping("/{materialId}/bom-excel/get")
    public ResponseEntity<?> getBomExcel(@PathVariable Long materialId) {
        try {
            ServiceResponse response = materialBomExcelService.getBomExcel(materialId);
            
            if ("ERROR".equals(response.getStatus())) {
                if (response.getStatusMsg().contains("not found")) {
                    return ResponseEntity.status(404).body(response);
                } else {
                    return ResponseEntity.status(500).body(response);
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting BOM Excel file: {}", e.getMessage(), e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error getting BOM Excel file: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Download BOM Excel file for a material
     * GET /api/materials/{materialId}/bom-excel/download
     */
    @GetMapping("/{materialId}/bom-excel/download")
    public ResponseEntity<?> downloadBomExcel(@PathVariable Long materialId) {
        try {
            byte[] fileBytes = materialBomExcelService.getBomExcelFileBytes(materialId);
            String fileName = materialBomExcelService.getBomExcelFileName(materialId);
            
            if (fileName == null) {
                fileName = "bom_material_" + materialId + ".xlsx";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(fileBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);
                    
        } catch (RuntimeException e) {
            logger.error("Error downloading BOM Excel file: {}", e.getMessage(), e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg(e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(errorResponse);
            }
            return ResponseEntity.status(500).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error downloading BOM Excel file: {}", e.getMessage(), e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error downloading BOM Excel file: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
} 