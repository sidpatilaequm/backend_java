package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderListResponse;
import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderRequest;
import com.example.multimedia.file_upload_api.dto.PortalPurchaseOrderResponse;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.service.PortalPurchaseOrderService;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortalPurchaseOrderServiceImpl implements PortalPurchaseOrderService {

    @Autowired
    private PortalPurchaseOrderRepository poRepository;

    @Autowired
    private PortalPurchaseOrderItemRepository poItemRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.VendorMasterRepository vendorMasterRepository;

    @Autowired
    private VendorQuotationRepository quotationRepository;

    @Autowired
    private PurchaseRequisitionRepository prRepository;

    @Autowired
    private FinancialTermsRepository financialTermsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.EmployeeRepository employeeRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.UserDetailRepository userDetailRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.AsnItemRepository asnItemRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.security.OrgConfigGate orgConfigGate;

    @Override
    @Transactional
    public PortalPurchaseOrderResponse createPOFromAwardedQuotation(Long quotationId, PortalPurchaseOrderRequest request) {
        orgConfigGate.requirePrToPoEnabled();

        // Find quotation
        VendorQuotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));

        // Validate quotation is AWARDED
        if (!"AWARDED".equalsIgnoreCase(quotation.getStatus())) {
            throw new IllegalArgumentException("Purchase Order can only be created from an AWARDED quotation.");
        }

        // Validate PO is not already created for this quotation
        Optional<PortalPurchaseOrder> existingPO = poRepository.findByPoNumber("PO-" + quotation.getQuotationNumber());
        // Or check by quotation id directly using custom filter
        List<PortalPurchaseOrder> checkList = poRepository.findByVendor_CompanyIdOrderByIdDesc(quotation.getVendor().getCompanyId(), null);
        for (PortalPurchaseOrder po : checkList) {
            if (po.getQuotation() != null && po.getQuotation().getQuotationId().equals(quotationId)) {
                throw new IllegalArgumentException("Purchase Order has already been created for this quotation.");
            }
        }

        PurchaseRequisition pr = quotation.getPurchaseRequisition();
        if (pr == null) {
            throw new RuntimeException("Purchase Requisition associated with quotation not found.");
        }

        // Generate sequential PO number
        long count = poRepository.count();
        String poNumber = "PO-" + LocalDate.now().getYear() + "-" + String.format("%04d", count + 1);

        // Header Freight Calculation
        BigDecimal headerFreight = quotation.getFreightAmount() != null ? quotation.getFreightAmount() : BigDecimal.ZERO;
        BigDecimal itemFreightTotal = BigDecimal.ZERO;
        if (quotation.getItems() != null) {
            itemFreightTotal = quotation.getItems().stream()
                    .map(i -> i.getFreightAmount() != null ? i.getFreightAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        BigDecimal freightTotal = headerFreight.add(itemFreightTotal);

        // Create PO Header
        PortalPurchaseOrder po = new PortalPurchaseOrder();
        po.setPoNumber(poNumber);
        po.setPoDate(LocalDate.now());
        po.setPurchaseRequisition(pr);
        po.setQuotation(quotation);
        po.setCompanyCode(pr.getCompanyCode());
        // Resolve the true vendor by BP No if companyCode is missing
        CompanyDetails quoteVendor = quotation.getVendor();
        CompanyDetails finalVendor = quoteVendor;
        
        if (quoteVendor.getCompanyCode() == null) {
            java.util.Optional<com.example.multimedia.file_upload_api.entity.VendorMaster> vmOpt = vendorMasterRepository.findById(quoteVendor.getCompanyId());
            if (vmOpt.isPresent() && vmOpt.get().getBpNo() != null) {
                List<CompanyDetails> matches = companyDetailsRepository.findByCompanyCode(vmOpt.get().getBpNo());
                if (!matches.isEmpty()) {
                    finalVendor = matches.get(0);
                }
            }
        }

        po.setVendor(finalVendor);
        po.setCurrency(quotation.getCurrency() != null ? quotation.getCurrency() : "INR");
        po.setPaymentTermsId(quotation.getPaymentTermsId());
        po.setDeliveryAddress(request != null ? request.getDeliveryAddress() : null);
        po.setRequestedDeliveryDate(pr.getRequiredDate());
        po.setConfirmedDeliveryDate(quotation.getQuotedDeliveryDate());
        po.setShippingInstructions(request != null ? request.getShippingInstructions() : null);
        po.setRemarks(request != null ? request.getRemarks() : null);
        po.setStatus("CREATED");
        po.setSubtotal(quotation.getSubtotalAmount());
        po.setGstTotal(quotation.getGstTotalAmount());
        po.setFreightTotal(freightTotal);
        po.setGrandTotal(quotation.getGrandTotalAmount());

        // Create PO Items
        List<PortalPurchaseOrderItem> poItems = new ArrayList<>();
        int lineNum = 10;
        if (quotation.getItems() != null) {
            for (VendorQuotationItem quoteItem : quotation.getItems()) {
                PortalPurchaseOrderItem poItem = new PortalPurchaseOrderItem();
                poItem.setPurchaseOrder(po);
                poItem.setLineNumber(lineNum);
                poItem.setMaterialNumber(quoteItem.getItemCode());
                poItem.setMaterialDescription(quoteItem.getDescription());
                poItem.setQuantity(quoteItem.getQuotedQty() != null ? quoteItem.getQuotedQty() : BigDecimal.ZERO);
                poItem.setUom(quoteItem.getUom());
                poItem.setUnitPrice(quoteItem.getUnitPrice() != null ? quoteItem.getUnitPrice() : BigDecimal.ZERO);
                poItem.setNetValue(quoteItem.getLineTotal() != null ? quoteItem.getLineTotal() : BigDecimal.ZERO);
                poItem.setTaxPercent(quoteItem.getGstPercent());
                poItem.setTaxAmount(quoteItem.getGstAmount());
                
                BigDecimal netValue = quoteItem.getLineTotal() != null ? quoteItem.getLineTotal() : BigDecimal.ZERO;
                BigDecimal taxAmount = quoteItem.getGstAmount() != null ? quoteItem.getGstAmount() : BigDecimal.ZERO;
                poItem.setTotalValue(netValue.add(taxAmount));

                poItems.add(poItem);
                lineNum += 10;
            }
        }
        po.setItems(poItems);

        // Save PO (cascades to items)
        po = poRepository.save(po);

        // Update PR Status to PO_CREATED
        pr.setStatus(PurchaseRequisitionStatus.PO_CREATED);
        prRepository.save(pr);

        return mapToResponse(po);
    }

    @Override
    public List<PortalPurchaseOrderListResponse> getAllPOsForAdmin(Long adminId) {
        if (currentUserService.isCurrentUserSuperAdmin()) {
            Long superAdminId = currentUserService.getCurrentSuperAdminId();
            List<PortalPurchaseOrder> pos = poRepository.findByVendor_SuperAdmin_SuperAdminIdOrderByIdDesc(superAdminId);
            return pos.stream().map(this::mapToListResponse).collect(Collectors.toList());
        } else {
            UserDetail user = currentUserService.getCurrentUser();
            List<Long> allowedUserIds = new java.util.ArrayList<>();
            allowedUserIds.add(user.getUserId());

            Optional<Employee> empOpt = employeeRepository.findByEmail(user.getEmail());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                List<Employee> subordinates = employeeRepository.findByManager_EmployeeCode(emp.getEmployeeCode());
                if (subordinates != null) {
                    for (Employee sub : subordinates) {
                        userDetailRepository.findByEmail(sub.getEmail()).ifPresent(subUser -> {
                            allowedUserIds.add(subUser.getUserId());
                        });
                    }
                }
            }

            java.util.List<String> createdByList = allowedUserIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList());
            List<PortalPurchaseOrder> pos = poRepository.findEmployeePOs(user.getSuperAdmin().getSuperAdminId(), allowedUserIds, createdByList);
            return pos.stream().map(this::mapToListResponse).collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public PortalPurchaseOrderResponse getPODetailsForAdmin(Long poId, Long adminId) {
        PortalPurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with ID: " + poId));

        if (currentUserService.isCurrentUserSuperAdmin()) {
            Long superAdminId = currentUserService.getCurrentSuperAdminId();
            if (!po.getVendor().getSuperAdmin().getSuperAdminId().equals(superAdminId)) {
                throw new SecurityException("Unauthorized: Purchase Order does not belong to this administrator's tenant.");
            }
        } else {
            UserDetail user = currentUserService.getCurrentUser();
            List<Long> allowedUserIds = new java.util.ArrayList<>();
            allowedUserIds.add(user.getUserId());

            Optional<Employee> empOpt = employeeRepository.findByEmail(user.getEmail());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                List<Employee> subordinates = employeeRepository.findByManager_EmployeeCode(emp.getEmployeeCode());
                if (subordinates != null) {
                    for (Employee sub : subordinates) {
                        userDetailRepository.findByEmail(sub.getEmail()).ifPresent(subUser -> {
                            allowedUserIds.add(subUser.getUserId());
                        });
                    }
                }
            }

            boolean authorized = false;
            if (po.getPurchaseRequisition() != null && allowedUserIds.contains(po.getPurchaseRequisition().getRequestedBy())) {
                authorized = true;
            }
            if (po.getCreatedBy() != null) {
                try {
                    if (allowedUserIds.contains(Long.parseLong(po.getCreatedBy()))) {
                        authorized = true;
                    }
                } catch (NumberFormatException ignored) {}
            } else if (po.getVendor() != null && po.getVendor().getSuperAdmin() != null && 
                       po.getVendor().getSuperAdmin().getSuperAdminId().equals(user.getSuperAdmin().getSuperAdminId())) {
                authorized = true;
            }

            if (!authorized) {
                throw new SecurityException("Unauthorized to view this Purchase Order.");
            }
        }

        return mapToResponse(po);
    }

    @Override
    @Transactional
    public void cancelPO(Long poId, Long adminId) {
        PortalPurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with ID: " + poId));

        if (currentUserService.isCurrentUserSuperAdmin()) {
            Long superAdminId = currentUserService.getCurrentSuperAdminId();
            if (!po.getVendor().getSuperAdmin().getSuperAdminId().equals(superAdminId)) {
                throw new SecurityException("Unauthorized: Purchase Order does not belong to this administrator's tenant.");
            }
        } else {
            UserDetail user = currentUserService.getCurrentUser();
            boolean authorized = false;
            if (po.getPurchaseRequisition() != null && user.getUserId().equals(po.getPurchaseRequisition().getRequestedBy())) {
                authorized = true;
            }
            if (po.getCreatedBy() != null) {
                try {
                    if (user.getUserId().equals(Long.parseLong(po.getCreatedBy()))) {
                        authorized = true;
                    }
                } catch (NumberFormatException ignored) {}
            }
            if (!authorized) {
                throw new SecurityException("Unauthorized to cancel this Purchase Order.");
            }
        }

        po.setStatus("CANCELLED");
        poRepository.save(po);
    }

    @Override
    public List<PortalPurchaseOrderListResponse> getPOsForVendor(Long vendorId, String companyCode) {
        String bpNo = companyDetailsRepository.findById(vendorId)
                .map(com.example.multimedia.file_upload_api.entity.CompanyDetails::getCompanyCode)
                .orElse(null);

        if (bpNo != null) {
            List<PortalPurchaseOrder> pos = poRepository.findByVendor_CompanyCodeOrderByIdDesc(bpNo);
            // Additionally filter by companyCode if passed, because the above method doesn't do it.
            // But wait, the PR actually uses companyCode, let's just filter it in memory to be safe, or 
            // since we added companyCode to findByVendor_CompanyIdOrderByIdDesc we can use that fallback.
            if (companyCode != null) {
                pos = pos.stream().filter(p -> companyCode.equals(p.getCompanyCode())).collect(Collectors.toList());
            }
            return pos.stream().map(this::mapToListResponse).collect(Collectors.toList());
        } else {
            // Fallback just in case bpNo is not set
            List<PortalPurchaseOrder> pos = poRepository.findByVendor_CompanyIdOrderByIdDesc(vendorId, companyCode);
            return pos.stream().map(this::mapToListResponse).collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public PortalPurchaseOrderResponse getPODetailsForVendor(Long poId, Long vendorId) {
        PortalPurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found for ID: " + poId));

        String bpNo = companyDetailsRepository.findById(vendorId)
                .map(com.example.multimedia.file_upload_api.entity.CompanyDetails::getCompanyCode)
                .orElse(null);

        boolean authorized = false;
        if (po.getVendor() != null) {
            if (po.getVendor().getCompanyId().equals(vendorId)) {
                authorized = true;
            } else if (bpNo != null && bpNo.equals(po.getVendor().getCompanyCode())) {
                authorized = true;
            }
        }

        if (!authorized) {
            throw new RuntimeException("Purchase Order not found or unauthorized for ID: " + poId);
        }

        return mapToResponse(po);
    }

    private PortalPurchaseOrderListResponse mapToListResponse(PortalPurchaseOrder po) {
        PortalPurchaseOrderListResponse res = new PortalPurchaseOrderListResponse();
        res.setPoId(po.getId());
        res.setPoNumber(po.getPoNumber());
        res.setPoDate(po.getPoDate());
        res.setStatus(po.getStatus());
        res.setGrandTotal(po.getGrandTotal());
        return res;
    }

    private PortalPurchaseOrderResponse mapToResponse(PortalPurchaseOrder po) {
        PortalPurchaseOrderResponse res = new PortalPurchaseOrderResponse();
        res.setPoId(po.getId());
        res.setPoNumber(po.getPoNumber());
        res.setPoDate(po.getPoDate());

        if (po.getPurchaseRequisition() != null) {
            res.setPrId(po.getPurchaseRequisition().getId());
            res.setPrNumber(po.getPurchaseRequisition().getPrNumber());
        }

        if (po.getQuotation() != null) {
            res.setQuotationId(po.getQuotation().getQuotationId());
            res.setQuotationNumber(po.getQuotation().getQuotationNumber());
        }

        // Vendor Info
        CompanyDetails vendor = po.getVendor();
        if (vendor != null) {
            PortalPurchaseOrderResponse.VendorInfo vInfo = new PortalPurchaseOrderResponse.VendorInfo();
            vInfo.setVendorId(vendor.getCompanyId());
            vInfo.setVendorCode(vendor.getCompanyCode());
            vInfo.setVendorName(vendor.getCompanyName());
            vInfo.setGstin(vendor.getGstinNumber());
            res.setVendor(vInfo);
        }

        res.setCurrency(po.getCurrency());

        // Payment Terms Info
        if (po.getPaymentTermsId() != null) {
            PortalPurchaseOrderResponse.PaymentTermsInfo ptInfo = new PortalPurchaseOrderResponse.PaymentTermsInfo();
            ptInfo.setPaymentTermsId(po.getPaymentTermsId());
            financialTermsRepository.findById(po.getPaymentTermsId()).ifPresent(terms -> {
                ptInfo.setName(terms.getTermsOfPayment());
            });
            res.setPaymentTerms(ptInfo);
        }

        res.setDeliveryAddress(po.getDeliveryAddress());
        res.setRequestedDeliveryDate(po.getRequestedDeliveryDate());
        res.setConfirmedDeliveryDate(po.getConfirmedDeliveryDate());
        res.setShippingInstructions(po.getShippingInstructions());
        res.setStatus(po.getStatus());

        res.setSubtotal(po.getSubtotal());
        res.setGstTotal(po.getGstTotal());
        res.setFreightTotal(po.getFreightTotal());
        res.setGrandTotal(po.getGrandTotal());

        // Items Info
        if (po.getItems() != null) {
            List<PortalPurchaseOrderResponse.ItemInfo> itemInfoList = po.getItems().stream().map(i -> {
                PortalPurchaseOrderResponse.ItemInfo itemRes = new PortalPurchaseOrderResponse.ItemInfo();
                itemRes.setLineNumber(i.getLineNumber());
                itemRes.setMaterialNumber(i.getMaterialNumber());
                itemRes.setMaterialDescription(i.getMaterialDescription());
                itemRes.setQuantity(i.getQuantity());
                itemRes.setUom(i.getUom());
                itemRes.setUnitPrice(i.getUnitPrice());
                itemRes.setNetValue(i.getNetValue());
                itemRes.setTaxPercent(i.getTaxPercent());
                itemRes.setTaxAmount(i.getTaxAmount());
                itemRes.setTotalValue(i.getTotalValue());
                
                BigDecimal received = asnItemRepository.getReceivedQuantity(po.getPoNumber(), i.getLineNumber());
                if (received == null) received = BigDecimal.ZERO;
                itemRes.setReceivedQuantity(received);
                
                BigDecimal inTransit = asnItemRepository.getInTransitQuantity(po.getPoNumber(), i.getLineNumber());
                if (inTransit == null) inTransit = BigDecimal.ZERO;
                itemRes.setInTransitQuantity(inTransit);
                
                BigDecimal pending = i.getQuantity() != null ? i.getQuantity().subtract(received).subtract(inTransit) : BigDecimal.ZERO;
                itemRes.setPendingQuantity(pending.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pending);
                
                return itemRes;
            }).collect(Collectors.toList());
            res.setItems(itemInfoList);
        }

        res.setCreatedAt(po.getCreatedDate());
        return res;
    }

    @Override
    @Transactional
    public void acknowledgePO(Long poId, Long vendorId) {
        PortalPurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with ID: " + poId));
        
        String bpNo = companyDetailsRepository.findById(vendorId)
                .map(com.example.multimedia.file_upload_api.entity.CompanyDetails::getCompanyCode)
                .orElse(null);

        boolean authorized = false;
        if (po.getVendor() != null) {
            if (po.getVendor().getCompanyId().equals(vendorId)) {
                authorized = true;
            } else if (bpNo != null && bpNo.equals(po.getVendor().getCompanyCode())) {
                authorized = true;
            }
        }

        if (!authorized) {
            throw new RuntimeException("Unauthorized: You do not have permission to acknowledge this PO (Vendor not assigned or mismatch)");
        }

        if (!"RELEASED".equalsIgnoreCase(po.getStatus()) && !"CREATED".equalsIgnoreCase(po.getStatus()) && !"APPROVED".equalsIgnoreCase(po.getStatus())) {
            throw new RuntimeException("Purchase Order is not in a valid state to be acknowledged. Current state: " + po.getStatus());
        }
        po.setStatus("ACKNOWLEDGED");
        po.setAcknowledgedAt(java.time.LocalDateTime.now());
        poRepository.save(po);
    }
}
