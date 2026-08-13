package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.MaterialMaster;
import com.example.multimedia.file_upload_api.repository.MaterialMasterRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialMasterService {

    @Autowired
    private MaterialMasterRepository repository;

    public void saveExcelData(MultipartFile file) throws Exception {
        List<MaterialMaster> materialMasters = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue; // Skip header row
                }

                MaterialMaster materialMaster = new MaterialMaster();

                materialMaster.setInfoRec(getCellValueAsString(row.getCell(0)));
                materialMaster.setMaterial(getCellValueAsString(row.getCell(1)));
                materialMaster.setMaterialDescription(getCellValueAsString(row.getCell(2)));
                materialMaster.setType(getCellValueAsString(row.getCell(3)));
                materialMaster.setMatTypeDesc(getCellValueAsString(row.getCell(4)));
                materialMaster.setGroupName(getCellValueAsString(row.getCell(5)));
                materialMaster.setUnit(getCellValueAsString(row.getCell(6)));
                materialMaster.setVendor(getCellValueAsString(row.getCell(7)));
                materialMaster.setVendorName(getCellValueAsString(row.getCell(8)));
                materialMaster.setPurOrg(getCellValueAsString(row.getCell(9)));
                materialMaster.setPlant(getCellValueAsString(row.getCell(10)));
                materialMaster.setName1(getCellValueAsString(row.getCell(11)));

                String priceStr = getCellValueAsString(row.getCell(12));
                if (priceStr != null && !priceStr.isEmpty()) {
                    try {
                        materialMaster.setPrice(Double.parseDouble(priceStr.replace(",", "")));
                    } catch (NumberFormatException e) {
                        materialMaster.setPrice(0.0);
                    }
                } else {
                    materialMaster.setPrice(0.0);
                }

                materialMaster.setCurr(getCellValueAsString(row.getCell(13)));
                materialMaster.setCoCode(getCellValueAsString(row.getCell(14)));

                // User ID 0 as requested for upload
                materialMaster.setUserId(0L);

                materialMasters.add(materialMaster);
            }
        }

        repository.saveAll(materialMasters);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.format("%d", (long) numericValue);
                } else {
                    return String.format("%s", numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    public List<MaterialMaster> getMaterialsByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}
