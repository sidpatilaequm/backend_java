package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.repository.BudgetVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final BudgetVersionRepository budgetVersionRepository;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("activeBudgetsCount", budgetVersionRepository.count()); // simplified
        data.put("pendingPrApprovals", 5); // placeholder for now
        data.put("recentActivities", "System updated"); // placeholder
        return data;
    }
}
