package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.BudgetDTOs;
import com.example.multimedia.file_upload_api.entity.BudgetVersion;
import com.example.multimedia.file_upload_api.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping("/versions")
    public ResponseEntity<List<BudgetDTOs.BudgetVersionResponse>> getBudgetVersions() {
        return ResponseEntity.ok(budgetService.getAllBudgetVersions());
    }

    @PostMapping("/upload")
    public ResponseEntity<BudgetDTOs.BudgetVersionResponse> uploadBudget(@RequestBody BudgetDTOs.BudgetUploadRequest request) {
        BudgetVersion version = budgetService.uploadBudget(request);
        
        BudgetDTOs.BudgetVersionResponse response = new BudgetDTOs.BudgetVersionResponse();
        response.setVersionCode(version.getVersionCode());
        response.setFiscalYear(version.getFiscalYear());
        response.setTotalAmount(version.getTotalAmount());
        response.setActive(version.isActive());
        response.setLocked(version.isLocked());
        
        return ResponseEntity.ok(response);
    }
}
