package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.PurchaseOrder;
import com.example.multimedia.file_upload_api.repository.sap.PurchaseOrderRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository repository;

    @Autowired
    private SecurityContextUtils securityContextUtils;
    
    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Override
    public SapSyncResponse syncPurchaseOrders() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        
        PurchaseOrder mockPo = new PurchaseOrder();
        mockPo.setVendorId(vendorId);
        mockPo.setPoNumber("PO-" + System.currentTimeMillis());
        mockPo.setPoStatus("OPEN");
        mockPo.setCompanyName("ABC Ltd");
        mockPo.setPoDate(LocalDate.now());
        mockPo.setSyncStatus("SUCCESS");
        
        repository.save(mockPo);

        return new SapSyncResponse("SUCCESS", "PO Sync Completed", 1);
    }

    @Override
    public List<PurchaseOrder> getVendorPurchaseOrders() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return repository.findByVendorIdOrderBySyncedAtDesc(vendorId);
    }

    @Override
    public PurchaseOrder getVendorPurchaseOrder(String poNumber) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return repository.findByPoNumberAndVendorId(poNumber, vendorId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found or unauthorized"));
    }

    @Override
    public List<PurchaseOrder> getAdminPurchaseOrders() {
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
    public PurchaseOrder getAdminPurchaseOrder(String poNumber) {
        Long adminId = securityContextUtils.getCurrentAdminId();
        
        List<Long> mappedVendorIds = companyDetailsRepository.findBySuperAdminSuperAdminId(adminId).stream()
                .map(CompanyDetails::getCompanyId)
                .collect(Collectors.toList());

        if (mappedVendorIds.isEmpty()) {
            throw new RuntimeException("No mapped vendors found for admin");
        }

        return repository.findByPoNumberAndVendorIdIn(poNumber, mappedVendorIds)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found or unauthorized"));
    }
}
