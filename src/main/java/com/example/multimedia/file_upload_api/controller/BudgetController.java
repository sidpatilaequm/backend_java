package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.BudgetDTOs;
import com.example.multimedia.file_upload_api.entity.BudgetVersion;
import com.example.multimedia.file_upload_api.service.AuditLogService;
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
    private final AuditLogService auditLogService;

    @GetMapping("/versions")
    public ResponseEntity<List<BudgetDTOs.BudgetVersionResponse>> getBudgetVersions() {
        return ResponseEntity.ok(budgetService.getAllBudgetVersions());
    }

    @PostMapping("/upload")
    public ResponseEntity<BudgetDTOs.BudgetVersionResponse> uploadBudget(@RequestBody BudgetDTOs.BudgetUploadRequest request) {
        BudgetVersion version = budgetService.uploadBudget(request);

        auditLogService.recordGeneric("BUDGET_UPLOADED", version.getVersionCode(), List.of(
                new AuditLogService.FieldChange("fiscalYear", null, version.getFiscalYear()),
                new AuditLogService.FieldChange("totalAmount", null, String.valueOf(version.getTotalAmount()))
        ));

        BudgetDTOs.BudgetVersionResponse response = new BudgetDTOs.BudgetVersionResponse();
        response.setVersionCode(version.getVersionCode());
        response.setFiscalYear(version.getFiscalYear());
        response.setTotalAmount(version.getTotalAmount());
        response.setActive(version.isActive());
        response.setLocked(version.isLocked());
        
        return ResponseEntity.ok(response);
    }
}
