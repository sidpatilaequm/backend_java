package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.InventoryService;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * GET /api/inventory - Returns list of materials with inventory data
     * Query param: location (optional) - Filter by location name
     */
    @GetMapping
    public ResponseEntity<ServiceResponse> getAllInventory(@RequestParam(required = false) String location) {
        ServiceResponse response = inventoryService.getAllInventory(location);

        // Return 400 Bad Request for error responses
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/inventory/{material_id}/stock - Update single material stock
     * Body: { "stockQuantity": 100, "price": 99.99, "location": "JP Nagar" }
     */
    @PostMapping({ "/{materialId}/stock", "/{materialId}/stock/" })
    @PutMapping({ "/{materialId}/stock", "/{materialId}/stock/" })
    public ResponseEntity<ServiceResponse> updateStock(
            @PathVariable("materialId") Long materialId,
            @RequestBody Map<String, Object> request) {

        try {
            // Parse stockQuantity
            Integer stockQuantity = null;
            if (request.get("stockQuantity") != null) {
                try {
                    Object stockValue = request.get("stockQuantity");
                    if (stockValue instanceof Number) {
                        stockQuantity = ((Number) stockValue).intValue();
                    } else {
                        stockQuantity = Integer.valueOf(stockValue.toString());
                    }
                } catch (NumberFormatException e) {
                    ServiceResponse errorResponse = new ServiceResponse();
                    errorResponse.setStatus(AppConstants.ERRORCODE);
                    errorResponse.setErrorCode(AppConstants.ERRORCODE);
                    errorResponse.setStatusMsg("Invalid stockQuantity format. Must be a number.");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            // Parse price
            BigDecimal price = null;
            if (request.get("price") != null) {
                try {
                    Object priceValue = request.get("price");
                    if (priceValue instanceof Number) {
                        price = BigDecimal.valueOf(((Number) priceValue).doubleValue());
                    } else {
                        price = new BigDecimal(priceValue.toString());
                    }
                } catch (NumberFormatException e) {
                    ServiceResponse errorResponse = new ServiceResponse();
                    errorResponse.setStatus(AppConstants.ERRORCODE);
                    errorResponse.setErrorCode(AppConstants.ERRORCODE);
                    errorResponse.setStatusMsg("Invalid price format. Must be a number.");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            String location = request.get("location") != null ? request.get("location").toString() : null;

            ServiceResponse response = inventoryService.updateStock(materialId, stockQuantity, price, location);

            // Return 400 Bad Request for error responses
            if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Log the error to console
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus(AppConstants.ERRORCODE);
            errorResponse.setErrorCode(AppConstants.ERRORCODE);
            errorResponse.setStatusMsg("Error processing request: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * POST /api/inventory/bulk-update-stock - Bulk update stock by SKU
     * Body: [{ "sku": "PA001", "variantCode": "V001", "location": "JP Nagar",
     * "stockQuantity": 100, "price": 99.99 }]
     */
    @PostMapping("/bulk-update-stock")
    public ResponseEntity<ServiceResponse> bulkUpdateStock(@RequestBody List<Map<String, Object>> updates) {
        ServiceResponse response = inventoryService.bulkUpdateStock(updates);

        // Return 400 Bad Request for error responses
        if (response.getErrorCode() != null && !response.getErrorCode().equals("0")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
