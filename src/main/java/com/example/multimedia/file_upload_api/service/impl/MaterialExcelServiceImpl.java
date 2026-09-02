package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.UploadResponse;
import com.example.multimedia.file_upload_api.entity.ItemCategory;
import com.example.multimedia.file_upload_api.entity.ItemSubcategory;
import com.example.multimedia.file_upload_api.entity.Location;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.ItemCategoryRepository;
import com.example.multimedia.file_upload_api.repository.ItemSubcategoryRepository;
import com.example.multimedia.file_upload_api.repository.LocationRepository;
import com.example.multimedia.file_upload_api.repository.MaterialRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.service.MaterialExcelService;
import com.example.multimedia.file_upload_api.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialExcelServiceImpl implements MaterialExcelService {

    private final MaterialRepository materialRepository;
    private final SuperAdminRepository superAdminRepository;
    private final LocationRepository locationRepository;

    @Override
    public UploadResponse uploadMaterialMaster(MultipartFile file) {
        int totalRows = 0;
        int inserted = 0;
        int updated = 0;
        int failed = 0;

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Map<String, Material> materialMap = new HashMap<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Fetch defaults to prevent foreign key null constraint errors

            SuperAdmin defaultSuperAdmin = superAdminRepository.findAll().stream().findFirst().orElse(null);
            Location defaultLocation = locationRepository.findAll().stream().findFirst().orElse(null);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Skip header
                }

                String materialCode = ExcelUtil.getString(row, 1);
                
                // If material code is completely blank, assume row is empty
                if (materialCode == null || materialCode.isEmpty()) {
                    continue;
                }

                totalRows++;

                try {
                    Material material = materialMap.computeIfAbsent(materialCode, key ->
                            materialRepository.findByMaterialCode(key).orElseGet(Material::new)
                    );

                    boolean isNew = material.getMaterialId() == null;

                    material.setInfoRec(ExcelUtil.getString(row, 0));
                    material.setMaterialCode(materialCode);
                    material.setSapMaterialDescription(ExcelUtil.getString(row, 2));
                    material.setSapType(ExcelUtil.getString(row, 3));
                    material.setMatTypeDesc(ExcelUtil.getString(row, 4));
                    material.setSapGroup(ExcelUtil.getString(row, 5));
                    material.setSapUnit(ExcelUtil.getString(row, 6));
                    material.setSapVendor(ExcelUtil.getString(row, 7));
                    material.setSapVendorName(ExcelUtil.getString(row, 8));
                    material.setPurOrg(ExcelUtil.getString(row, 9));
                    material.setPlant(ExcelUtil.getString(row, 10));
                    material.setSapName1(ExcelUtil.getString(row, 11));
                    material.setSapPrice(ExcelUtil.getNumeric(row, 12));
                    material.setSapCurrency(ExcelUtil.getString(row, 13));
                    material.setCompanyCode(ExcelUtil.getString(row, 14));

                    // Some defaults for existing material entity if new (to avoid nullable=false constraint exceptions)
                    if (isNew) {
                        material.setSku(materialCode);
                        material.setMaterialName(material.getSapMaterialDescription() != null ? material.getSapMaterialDescription() : "SAP Material");
                        material.setDescription(material.getSapMaterialDescription() != null ? material.getSapMaterialDescription() : "SAP Material");
                        material.setType(material.getSapType() != null ? material.getSapType() : "SAP");
                        material.setBaseUnitOfMeasure(material.getSapUnit() != null ? material.getSapUnit() : "EA");
                        material.setHsnCode("UNKNOWN");
                        material.setVendorArticleNumber(materialCode);
                        material.setBlocked(false);
                        material.setVariantMandatory(false);
                        
                        // Set foreign keys to avoid null constraints

                        if (defaultSuperAdmin != null) material.setSuperAdmin(defaultSuperAdmin);
                        if (defaultLocation != null) material.setLocation(defaultLocation);
                    }

                    // Tracking is already done by materialMap.
                    // Counting logic can be slightly tricky if the same material appears twice, but we'll increment if it's the first time we see it as New.
                    // If it was already in the map as new, `isNew` will be false for subsequent rows, which is fine for counts.

                    if (isNew) inserted++;
                    else updated++;

                } catch (Exception e) {
                    log.error("Failed to process row {}", row.getRowNum(), e);
                    failed++;
                }
            }

            materialRepository.saveAll(materialMap.values());

        } catch (Exception e) {
            log.error("Failed to parse Excel file", e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }

        return UploadResponse.builder()
                .status("SUCCESS")
                .totalRows(totalRows)
                .inserted(inserted)
                .updated(updated)
                .failed(failed)
                .build();
    }
}
