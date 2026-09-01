package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionRequest;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionStatusRequest;
import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import com.example.multimedia.file_upload_api.service.PurchaseRequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.multimedia.file_upload_api.repository.LocationRepository;
import com.example.multimedia.file_upload_api.repository.MaterialRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-requisitions")
public class PurchaseRequisitionController {

    @Autowired
    private PurchaseRequisitionService prService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @GetMapping("/create-pr-options")
    public ResponseEntity<Map<String, Object>> getCreatePrOptions() {
        Map<String, Object> response = new HashMap<>();
        response.put("locations", locationRepository.findAll());
        response.put("materials", materialRepository.findAll());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createPurchaseRequisition(
            @RequestBody PurchaseRequisitionRequest request) {
        PurchaseRequisitionResponse response = prService.createPurchaseRequisition(request);
        java.util.Map<String, Object> responseMap = new java.util.LinkedHashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "Purchase Requisition created successfully");
        responseMap.put("prId", response.getId());
        responseMap.put("prNumber", response.getPrNumber());
        responseMap.put("id", response.getId());
        responseMap.put("locationId", response.getLocationId());
        responseMap.put("locationName", response.getLocationName());
        responseMap.put("requestedBy", response.getRequestedBy());
        responseMap.put("requiredDate", response.getRequiredDate());
        responseMap.put("remarks", response.getRemarks());
        responseMap.put("status", response.getStatus());
        responseMap.put("totalAmount", response.getTotalAmount());
        responseMap.put("createdAt", response.getCreatedAt());
        responseMap.put("updatedAt", response.getUpdatedAt());
        responseMap.put("itemCount", response.getItemCount());
        responseMap.put("items", response.getItems());
        return new ResponseEntity<>(responseMap, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseRequisitionResponse>> getAllPurchaseRequisitions(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) PurchaseRequisitionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PurchaseRequisitionResponse> response = prService.getAllPurchaseRequisitions(locationId, status, search,
                pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseRequisitionResponse> getPurchaseRequisitionById(@PathVariable Long id) {
        PurchaseRequisitionResponse response = prService.getPurchaseRequisitionById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseRequisitionResponse> updatePurchaseRequisition(@PathVariable Long id,
            @RequestBody PurchaseRequisitionRequest request) {
        PurchaseRequisitionResponse response = prService.updatePurchaseRequisition(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> changePurchaseRequisitionStatus(@PathVariable Long id,
            @RequestBody PurchaseRequisitionStatusRequest statusRequest) {
        prService.changePurchaseRequisitionStatus(id, statusRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updatePurchaseRequisitionStatus(@PathVariable Long id,
            @RequestBody PurchaseRequisitionStatusRequest statusRequest) {
        prService.changePurchaseRequisitionStatus(id, statusRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pr-number/{prNumber}")
    public ResponseEntity<PurchaseRequisitionResponse> getPurchaseRequisitionByPrNumber(@PathVariable String prNumber) {
        PurchaseRequisitionResponse response = prService.getPurchaseRequisitionByPrNumber(prNumber);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseRequisition(@PathVariable Long id) {
        prService.deletePurchaseRequisition(id);
        return ResponseEntity.noContent().build();
    }
}
