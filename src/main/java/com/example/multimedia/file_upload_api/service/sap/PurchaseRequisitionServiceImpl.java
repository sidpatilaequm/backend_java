package com.example.multimedia.file_upload_api.service.sap;

import com.example.multimedia.file_upload_api.dto.sap.SapSyncResponse;
import com.example.multimedia.file_upload_api.entity.sap.PurchaseRequisition;
import com.example.multimedia.file_upload_api.repository.sap.PurchaseRequisitionRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service("sapPurchaseRequisitionServiceImpl")
public class PurchaseRequisitionServiceImpl implements PurchaseRequisitionService {

    @Autowired
    private PurchaseRequisitionRepository repository;

    @Autowired
    private SecurityContextUtils securityContextUtils;
    
    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Override
    public SapSyncResponse syncPurchaseRequisitions() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        
        // MOCK SAP API CALL
        // In reality, here we would make an HTTP call to SAP with the vendorId
        // and get a list of PRs to save. For demonstration, we'll create some mock data.
        
        PurchaseRequisition mockPr = new PurchaseRequisition();
        mockPr.setVendorId(vendorId);
        mockPr.setPrNumber("PR-" + System.currentTimeMillis());
        mockPr.setPrStatus("OPEN");
        mockPr.setMaterialDescription("Steel Rod");
        mockPr.setQuantity(new BigDecimal("500.00"));
        mockPr.setUom("KG");
        mockPr.setCreatedDate(LocalDate.now());
        mockPr.setSyncStatus("SUCCESS");
        
        repository.save(mockPr);

        return new SapSyncResponse("SUCCESS", "PR Sync Completed", 1);
    }

    @Override
    public List<PurchaseRequisition> getVendorPurchaseRequisitions() {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return repository.findByVendorIdOrderBySyncedAtDesc(vendorId);
    }

    @Override
    public PurchaseRequisition getVendorPurchaseRequisition(String prNumber) {
        Long vendorId = securityContextUtils.getCurrentVendorId();
        return repository.findByPrNumberAndVendorId(prNumber, vendorId)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found or unauthorized"));
    }

    @Override
    public List<PurchaseRequisition> getAdminPurchaseRequisitions() {
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
    public PurchaseRequisition getAdminPurchaseRequisition(String prNumber) {
        Long adminId = securityContextUtils.getCurrentAdminId();
        
        List<Long> mappedVendorIds = companyDetailsRepository.findBySuperAdminSuperAdminId(adminId).stream()
                .map(CompanyDetails::getCompanyId)
                .collect(Collectors.toList());

        if (mappedVendorIds.isEmpty()) {
            throw new RuntimeException("No mapped vendors found for admin");
        }

        return repository.findByPrNumberAndVendorIdIn(prNumber, mappedVendorIds)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found or unauthorized"));
    }
}
