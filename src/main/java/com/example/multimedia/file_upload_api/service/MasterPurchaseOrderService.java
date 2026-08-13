package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.MasterPurchaseOrder;
import com.example.multimedia.file_upload_api.repository.MasterPurchaseOrderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MasterPurchaseOrderService {

    @Autowired
    private MasterPurchaseOrderRepository repository;

    public void saveExcelData(MultipartFile file) throws Exception {
        List<MasterPurchaseOrder> orders = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue; // Skip header row
                }

                MasterPurchaseOrder order = new MasterPurchaseOrder();

                order.setDocNo(getCellValueAsString(row.getCell(0)));
                order.setDocCat(getCellValueAsString(row.getCell(1)));
                order.setCategory(getCellValueAsString(row.getCell(2)));
                order.setDocType(getCellValueAsString(row.getCell(3)));
                order.setDocTypeText(getCellValueAsString(row.getCell(4)));
                order.setVendor(getCellValueAsString(row.getCell(5)));
                order.setVendorName(getCellValueAsString(row.getCell(6)));
                order.setItem(getCellValueAsString(row.getCell(7)));
                order.setMaterialNumber(getCellValueAsString(row.getCell(8)));
                order.setShortText(getCellValueAsString(row.getCell(9)));

                String quantityStr = getCellValueAsString(row.getCell(10));
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    try {
                        order.setQuantity(Double.parseDouble(quantityStr.replace(",", "")));
                    } catch (NumberFormatException e) {
                        order.setQuantity(0.0);
                    }
                } else {
                    order.setQuantity(0.0);
                }

                order.setOrderUnit(getCellValueAsString(row.getCell(11)));
                order.setCoCode(getCellValueAsString(row.getCell(12)));

                // Default userId and adminId to 0
                order.setUserId(0L);
                order.setAdminId(0L);

                orders.add(order);
            }
        }

        repository.saveAll(orders);
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

    public List<MasterPurchaseOrder> getOrdersByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}
