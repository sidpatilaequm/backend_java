package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.SubconPurchaseOrder;
import com.example.multimedia.file_upload_api.repository.sap.SubconPurchaseOrderRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubconPurchaseOrderServiceImpl implements SubconPurchaseOrderService {

    @Autowired
    private SubconPurchaseOrderRepository repository;

    @Autowired
    private SecurityContextUtils securityContextUtils;
    
    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Override
    public SapSyncResponse syncSubconPurchaseOrders() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        
        SubconPurchaseOrder mockPo = new SubconPurchaseOrder();
        mockPo.setVendorId(vendorId);
        mockPo.setSubconPoNumber("SPO-" + System.currentTimeMillis());
        mockPo.setPoStatus("OPEN");
        mockPo.setCompanyName("Subcon Ltd");
        mockPo.setPoDate(LocalDate.now());
        mockPo.setSyncStatus("SUCCESS");
        
        repository.save(mockPo);

        return new SapSyncResponse("SUCCESS", "Subcon PO Sync Completed", 1);
    }

    @Override
    public List<SubconPurchaseOrder> getVendorSubconPurchaseOrders() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return repository.findByVendorIdOrderBySyncedAtDesc(vendorId);
    }

    @Override
    public SubconPurchaseOrder getVendorSubconPurchaseOrder(String subconPoNumber) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return repository.findBySubconPoNumberAndVendorId(subconPoNumber, vendorId)
                .orElseThrow(() -> new RuntimeException("Subcon Purchase Order not found or unauthorized"));
    }

    @Override
    public List<SubconPurchaseOrder> getAdminSubconPurchaseOrders() {
        Long adminId = securityContextUtils.getCurrentAdminId();
        
        List<Long> mappedVendorIds = companyDetailsRepository.findBySuperAdminSuperAdminId(adminId).stream()
                .map(CompanyDetails::getCompanyId)
                .collect(Collectors.toList());

        if (mappedVendorIds.isEmpty()) {
            return List.of();
        }
        
        return repository.findByVendorIdInOrderBySyncedAtDesc(mappedVendorIds);
    }

    @Override
    public SubconPurchaseOrder getAdminSubconPurchaseOrder(String subconPoNumber) {
        Long adminId = securityContextUtils.getCurrentAdminId();
        
        List<Long> mappedVendorIds = companyDetailsRepository.findBySuperAdminSuperAdminId(adminId).stream()
                .map(CompanyDetails::getCompanyId)
                .collect(Collectors.toList());

        if (mappedVendorIds.isEmpty()) {
            throw new RuntimeException("No mapped vendors found for admin");
        }

        return repository.findBySubconPoNumberAndVendorIdIn(subconPoNumber, mappedVendorIds)
                .orElseThrow(() -> new RuntimeException("Subcon Purchase Order not found or unauthorized"));
    }
}
