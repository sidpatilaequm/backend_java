package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.BudgetDTOs;
import com.example.multimedia.file_upload_api.entity.BudgetItem;
import com.example.multimedia.file_upload_api.entity.BudgetVersion;
import com.example.multimedia.file_upload_api.entity.DepartmentBudget;
import com.example.multimedia.file_upload_api.repository.BudgetVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetVersionRepository budgetVersionRepository;

    public List<BudgetDTOs.BudgetVersionResponse> getAllBudgetVersions() {
        return budgetVersionRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public BudgetVersion uploadBudget(BudgetDTOs.BudgetUploadRequest request) {
        if (budgetVersionRepository.findById(request.getVersionCode()).isPresent()) {
            throw new IllegalArgumentException("Budget version code already exists");
        }

        BudgetVersion version = new BudgetVersion();
        version.setVersionCode(request.getVersionCode());
        version.setFiscalYear(request.getFiscalYear());
        version.setTotalAmount(request.getTotalAmount());
        version.setActive(true);
        version.setLocked(false);

        List<DepartmentBudget> deptBudgets = new ArrayList<>();
        
        if (request.getDepartmentBudgets() != null) {
            for (Map.Entry<String, List<BudgetDTOs.BudgetItemDTO>> entry : request.getDepartmentBudgets().entrySet()) {
                String deptCode = entry.getKey();
                List<BudgetDTOs.BudgetItemDTO> items = entry.getValue();

                DepartmentBudget deptBudget = new DepartmentBudget();
                deptBudget.setBudgetVersion(version);
                deptBudget.setDepartmentCode(deptCode);

                BigDecimal deptTotal = BigDecimal.ZERO;
                List<BudgetItem> budgetItems = new ArrayList<>();

                for (BudgetDTOs.BudgetItemDTO itemDTO : items) {
                    BudgetItem item = new BudgetItem();
                    item.setDepartmentBudget(deptBudget);
                    item.setCategory(itemDTO.getCategory());
                    item.setAmount(itemDTO.getAmount());
                    budgetItems.add(item);
                    deptTotal = deptTotal.add(itemDTO.getAmount());
                }

                deptBudget.setBudgetItems(budgetItems);
                deptBudget.setTotalAmount(deptTotal);
                deptBudgets.add(deptBudget);
            }
        }
        
        version.setDepartmentBudgets(deptBudgets);
        return budgetVersionRepository.save(version);
    }

    private BudgetDTOs.BudgetVersionResponse mapToResponse(BudgetVersion version) {
        BudgetDTOs.BudgetVersionResponse response = new BudgetDTOs.BudgetVersionResponse();
        response.setVersionCode(version.getVersionCode());
        response.setFiscalYear(version.getFiscalYear());
        response.setTotalAmount(version.getTotalAmount());
        response.setActive(version.isActive());
        response.setLocked(version.isLocked());
        return response;
    }
}
