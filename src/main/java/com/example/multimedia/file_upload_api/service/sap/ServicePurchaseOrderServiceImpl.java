package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.ServicePurchaseOrder;
import com.example.multimedia.file_upload_api.repository.sap.ServicePurchaseOrderRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicePurchaseOrderServiceImpl implements ServicePurchaseOrderService {

    @Autowired
    private ServicePurchaseOrderRepository repository;

    @Autowired
    private SecurityContextUtils securityContextUtils;
    
    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Override
    public SapSyncResponse syncServicePurchaseOrders() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        
        ServicePurchaseOrder mockPo = new ServicePurchaseOrder();
        mockPo.setVendorId(vendorId);
        mockPo.setServicePoNumber("SRV-" + System.currentTimeMillis());
        mockPo.setPoStatus("OPEN");
        mockPo.setCompanyName("Service Ltd");
        mockPo.setPoDate(LocalDate.now());
        mockPo.setSyncStatus("SUCCESS");
        
        repository.save(mockPo);

        return new SapSyncResponse("SUCCESS", "Service PO Sync Completed", 1);
    }

    @Override
    public List<ServicePurchaseOrder> getVendorServicePurchaseOrders() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return repository.findByVendorIdOrderBySyncedAtDesc(vendorId);
    }

    @Override
    public ServicePurchaseOrder getVendorServicePurchaseOrder(String servicePoNumber) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return repository.findByServicePoNumberAndVendorId(servicePoNumber, vendorId)
                .orElseThrow(() -> new RuntimeException("Service Purchase Order not found or unauthorized"));
    }

    @Override
    public List<ServicePurchaseOrder> getAdminServicePurchaseOrders() {
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
    public ServicePurchaseOrder getAdminServicePurchaseOrder(String servicePoNumber) {
        Long adminId = securityContextUtils.getCurrentAdminId();
        
        List<Long> mappedVendorIds = companyDetailsRepository.findBySuperAdminSuperAdminId(adminId).stream()
                .map(CompanyDetails::getCompanyId)
                .collect(Collectors.toList());

        if (mappedVendorIds.isEmpty()) {
            throw new RuntimeException("No mapped vendors found for admin");
        }

        return repository.findByServicePoNumberAndVendorIdIn(servicePoNumber, mappedVendorIds)
                .orElseThrow(() -> new RuntimeException("Service Purchase Order not found or unauthorized"));
    }
}
