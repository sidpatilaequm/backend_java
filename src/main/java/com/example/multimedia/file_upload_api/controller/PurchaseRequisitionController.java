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

@RestController
@RequestMapping("/api/purchase-requisitions")
public class PurchaseRequisitionController {

    @Autowired
    private PurchaseRequisitionService prService;

    @PostMapping
    public ResponseEntity<PurchaseRequisitionResponse> createPurchaseRequisition(
            @RequestBody PurchaseRequisitionRequest request) {
        PurchaseRequisitionResponse response = prService.createPurchaseRequisition(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseRequisition(@PathVariable Long id) {
        prService.deletePurchaseRequisition(id);
        return ResponseEntity.noContent().build();
    }
}
