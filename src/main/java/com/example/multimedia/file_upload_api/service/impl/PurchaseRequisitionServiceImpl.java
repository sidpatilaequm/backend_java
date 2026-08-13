package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionItemRequest;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionItemResponse;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionRequest;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionStatusRequest;
import com.example.multimedia.file_upload_api.entity.PurchaseRequisition;
import com.example.multimedia.file_upload_api.entity.PurchaseRequisitionItem;
import com.example.multimedia.file_upload_api.entity.PurchaseRequisitionItemVendor;
import com.example.multimedia.file_upload_api.entity.Location;
import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import com.example.multimedia.file_upload_api.repository.PurchaseRequisitionRepository;
import com.example.multimedia.file_upload_api.dto.VendorPurchaseRequisitionItemResponse;
import com.example.multimedia.file_upload_api.repository.PurchaseRequisitionItemVendorRepository;
import com.example.multimedia.file_upload_api.repository.LocationRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.MaterialRepository;
import com.example.multimedia.file_upload_api.repository.FinancialTermsRepository;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.FinancialTerms;
import com.example.multimedia.file_upload_api.entity.Material;
import com.example.multimedia.file_upload_api.entity.VendorMaster;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.Employee;
import com.example.multimedia.file_upload_api.service.EmailService;
import com.example.multimedia.file_upload_api.service.CurrentUserService;
import com.example.multimedia.file_upload_api.service.PurchaseRequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PurchaseRequisitionServiceImpl implements PurchaseRequisitionService {

    @Autowired
    private PurchaseRequisitionRepository prRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PurchaseRequisitionItemVendorRepository vendorRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private FinancialTermsRepository financialTermsRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.VendorMasterRepository vendorMasterRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.UserDetailRepository userDetailRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.EmployeeRepository employeeRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.repository.SuperAdminRepository superAdminRepository;

    @Override
    @Transactional
    public PurchaseRequisitionResponse createPurchaseRequisition(PurchaseRequisitionRequest request) {
        Long superAdminId;
        Long requestedById;
        if (currentUserService.isCurrentUserSuperAdmin()) {
            superAdminId = currentUserService.getCurrentSuperAdminId();
            requestedById = superAdminId;
        } else {
            UserDetail user = currentUserService.getCurrentUser();
            superAdminId = user.getSuperAdmin().getSuperAdminId();
            requestedById = user.getUserId();
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Location ID"));
        if (!location.getSuperAdmin().getSuperAdminId().equals(superAdminId)) {
            throw new IllegalArgumentException("Location does not belong to the current admin.");
        }

        PurchaseRequisition pr = new PurchaseRequisition();
        pr.setLocationId(request.getLocationId());
        pr.setRequiredDate(request.getRequiredDate());
        pr.setRemarks(request.getRemarks());
        pr.setStatus(request.getStatus() != null ? request.getStatus() : PurchaseRequisitionStatus.CREATED);
        pr.setRequestedBy(requestedById);

        String nextPrNumber = generateNextPrNumber();
        pr.setPrNumber(nextPrNumber);

        BigDecimal totalAmount = BigDecimal.ZERO;
        boolean allReleased = true;
        boolean anyReleased = false;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PurchaseRequisitionItemRequest itemReq : request.getItems()) {
                materialRepository.findByMaterialIdAndSuperAdmin_SuperAdminId(itemReq.getMaterialId(), superAdminId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Material ID or Material does not belong to the current admin: " + itemReq.getMaterialId()));

                PurchaseRequisitionItem item = new PurchaseRequisitionItem();
                item.setPurchaseRequisition(pr);
                item.setMaterialId(itemReq.getMaterialId());
                item.setSku(itemReq.getSku());

                // Keep quantity safe from null pointer
                BigDecimal qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : BigDecimal.ZERO;
                item.setQuantity(qty);
                item.setUom(itemReq.getUom());

                BigDecimal price = itemReq.getEstimatedPrice() != null ? itemReq.getEstimatedPrice() : BigDecimal.ZERO;
                item.setEstimatedPrice(price);

                BigDecimal itemTotal = qty.multiply(price);
                item.setTotalPrice(itemTotal);
                totalAmount = totalAmount.add(itemTotal);

                if (itemReq.getVendorIds() != null && !itemReq.getVendorIds().isEmpty()) {
                    for (int i = 0; i < itemReq.getVendorIds().size(); i++) {
                        Long passedVendorId = itemReq.getVendorIds().get(i);
                        Long finalVendorId = passedVendorId;
                        
                        // Internal fallback: If the frontend passes VendorMaster ID instead of Company ID, map it safely
                        Optional<VendorMaster> vmOpt = vendorMasterRepository.findById(passedVendorId);
                        if (vmOpt.isPresent() && vmOpt.get().getEmail() != null) {
                            Optional<UserDetail> userOpt = userDetailRepository.findByEmail(vmOpt.get().getEmail());
                            if (userOpt.isPresent() && userOpt.get().getCompany() != null) {
                                finalVendorId = userOpt.get().getCompany().getCompanyId();
                            }
                        }

                        PurchaseRequisitionItemVendor vendor = new PurchaseRequisitionItemVendor();
                        vendor.setPurchaseRequisitionItem(item);
                        vendor.setVendorId(finalVendorId);
                        
                        // Also set the BP no if passed, or fall back to VendorMaster
                        String bpNo = null;
                        if (itemReq.getVendorBpNos() != null && itemReq.getVendorBpNos().size() > i) {
                            bpNo = itemReq.getVendorBpNos().get(i);
                        } else if (vmOpt.isPresent()) {
                            bpNo = vmOpt.get().getBpNo();
                        }
                        vendor.setBpNo(bpNo);
                        
                        item.getItemVendors().add(vendor);

                        // Send Email Notification
                        companyDetailsRepository.findById(finalVendorId).ifPresent(company -> {
                            if (company.getUser() != null) {
                                String subject = "New RFQ: Purchase Requisition Assigned";
                                String body = "Dear " + company.getCompanyName() + ",\n\n" +
                                        "You have been assigned to provide a quotation for a new item.\n" +
                                        "PR Number: " + pr.getPrNumber() + "\n" +
                                        "Item SKU: " + itemReq.getSku() + "\n" +
                                        "Quantity: " + qty + " " + itemReq.getUom() + "\n\n" +
                                        "Please log in to your vendor portal to submit your quotation.\n\n" +
                                        "Best regards,\nAdmin Team";
                                emailService.sendSimpleEmailToUserId(company.getUser().getUserId(), subject, body);
                            }
                        });
                    }
                    item.setStatus("RELEASED");
                    anyReleased = true;
                } else {
                    item.setStatus("CREATED");
                    allReleased = false;
                }

                pr.getItems().add(item);
            }
        } else {
            allReleased = false;
        }

        if (anyReleased) {
            if (allReleased) {
                pr.setStatus(PurchaseRequisitionStatus.RELEASED);
            } else {
                pr.setStatus(PurchaseRequisitionStatus.PARTIALLY_RELEASED);
            }
        } else {
            pr.setStatus(PurchaseRequisitionStatus.CREATED);
        }

        pr.setTotalAmount(totalAmount);

        PurchaseRequisition savedPr = prRepository.save(pr);
        return mapToResponse(savedPr);
    }

    private String generateNextPrNumber() {
        PurchaseRequisition lastPr = prRepository.findTopByOrderByIdDesc().orElse(null);
        int currentYear = LocalDate.now().getYear();

        if (lastPr == null) {
            return "PR-" + currentYear + "-0001";
        }

        String lastPrNumber = lastPr.getPrNumber();
        String[] parts = lastPrNumber.split("-");

        if (parts.length == 3) {
            try {
                int lastYear = Integer.parseInt(parts[1]);
                int nextSequence = Integer.parseInt(parts[2]) + 1;

                if (lastYear != currentYear) {
                    return "PR-" + currentYear + "-0001";
                }

                return String.format("PR-%d-%04d", currentYear, nextSequence);
            } catch (NumberFormatException e) {
                // Fallback if parsing fails
            }
        }

        return "PR-" + currentYear + "-0001";
    }

    @Override
    public Page<PurchaseRequisitionResponse> getAllPurchaseRequisitions(Long locationId,
            PurchaseRequisitionStatus status, String search, Pageable pageable) {
        if (currentUserService.isCurrentUserSuperAdmin()) {
            Long superAdminId = currentUserService.getCurrentSuperAdminId();
            Page<PurchaseRequisition> prPage = prRepository.findWithFilters(superAdminId, locationId, status, search,
                    pageable);
            return prPage.map(this::mapToResponseWithoutItems);
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

            Page<PurchaseRequisition> prPage = prRepository.findWithFiltersIn(allowedUserIds, locationId, status, search,
                    pageable);
            return prPage.map(this::mapToResponseWithoutItems);
        }
    }

    @Override
    public PurchaseRequisitionResponse getPurchaseRequisitionById(Long id) {
        PurchaseRequisition pr = prRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found with id: " + id));
        return mapToResponse(pr);
    }

    @Override
    @Transactional
    public PurchaseRequisitionResponse updatePurchaseRequisition(Long id, PurchaseRequisitionRequest request) {
        Long superAdminId;
        Long currentUserId;
        if (currentUserService.isCurrentUserSuperAdmin()) {
            superAdminId = currentUserService.getCurrentSuperAdminId();
            currentUserId = superAdminId;
        } else {
            UserDetail user = currentUserService.getCurrentUser();
            superAdminId = user.getSuperAdmin().getSuperAdminId();
            currentUserId = user.getUserId();
        }

        PurchaseRequisition pr = prRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found with id: " + id));

        if (!pr.getRequestedBy().equals(currentUserId) && !currentUserService.isCurrentUserSuperAdmin()) {
            throw new IllegalArgumentException("You do not have permission to modify this Purchase Requisition.");
        }

        if (pr.getStatus() == PurchaseRequisitionStatus.RELEASED || pr.getStatus() == PurchaseRequisitionStatus.PARTIALLY_RELEASED) {
            throw new IllegalStateException("Cannot modify a Purchase Requisition that has already been dispatched to vendors.");
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Location ID"));
        if (!location.getSuperAdmin().getSuperAdminId().equals(superAdminId)) {
            throw new IllegalArgumentException("Location does not belong to the current admin.");
        }

        pr.setLocationId(request.getLocationId());
        pr.setRequiredDate(request.getRequiredDate());
        pr.setRemarks(request.getRemarks());

        pr.getItems().clear();

        BigDecimal totalAmount = BigDecimal.ZERO;
        boolean allReleased = true;
        boolean anyReleased = false;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PurchaseRequisitionItemRequest itemReq : request.getItems()) {
                materialRepository.findByMaterialIdAndSuperAdmin_SuperAdminId(itemReq.getMaterialId(), superAdminId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Material ID or Material does not belong to the current admin: " + itemReq.getMaterialId()));

                PurchaseRequisitionItem item = new PurchaseRequisitionItem();
                item.setPurchaseRequisition(pr);
                item.setMaterialId(itemReq.getMaterialId());
                item.setSku(itemReq.getSku());

                BigDecimal qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : BigDecimal.ZERO;
                item.setQuantity(qty);
                item.setUom(itemReq.getUom());

                BigDecimal price = itemReq.getEstimatedPrice() != null ? itemReq.getEstimatedPrice() : BigDecimal.ZERO;
                item.setEstimatedPrice(price);

                BigDecimal itemTotal = qty.multiply(price);
                item.setTotalPrice(itemTotal);
                totalAmount = totalAmount.add(itemTotal);

                if (itemReq.getVendorIds() != null && !itemReq.getVendorIds().isEmpty()) {
                    for (int i = 0; i < itemReq.getVendorIds().size(); i++) {
                        Long passedVendorId = itemReq.getVendorIds().get(i);
                        Long finalVendorId = passedVendorId;
                        
                        // Internal fallback: If the frontend passes VendorMaster ID instead of Company ID, map it safely
                        Optional<VendorMaster> vmOpt = vendorMasterRepository.findById(passedVendorId);
                        if (vmOpt.isPresent() && vmOpt.get().getEmail() != null) {
                            Optional<UserDetail> userOpt = userDetailRepository.findByEmail(vmOpt.get().getEmail());
                            if (userOpt.isPresent() && userOpt.get().getCompany() != null) {
                                finalVendorId = userOpt.get().getCompany().getCompanyId();
                            }
                        }

                        PurchaseRequisitionItemVendor vendor = new PurchaseRequisitionItemVendor();
                        vendor.setPurchaseRequisitionItem(item);
                        vendor.setVendorId(finalVendorId);
                        
                        // Also set the BP no if passed, or fall back to VendorMaster
                        String bpNo = null;
                        if (itemReq.getVendorBpNos() != null && itemReq.getVendorBpNos().size() > i) {
                            bpNo = itemReq.getVendorBpNos().get(i);
                        } else if (vmOpt.isPresent()) {
                            bpNo = vmOpt.get().getBpNo();
                        }
                        vendor.setBpNo(bpNo);
                        
                        item.getItemVendors().add(vendor);

                        // Send Email Notification
                        companyDetailsRepository.findById(finalVendorId).ifPresent(company -> {
                            if (company.getUser() != null) {
                                String subject = "New RFQ: Purchase Requisition Assigned";
                                String body = "Dear " + company.getCompanyName() + ",\n\n" +
                                        "You have been assigned to provide a quotation for a new item.\n" +
                                        "PR Number: " + pr.getPrNumber() + "\n" +
                                        "Item SKU: " + itemReq.getSku() + "\n" +
                                        "Quantity: " + qty + " " + itemReq.getUom() + "\n\n" +
                                        "Please log in to your vendor portal to submit your quotation.\n\n" +
                                        "Best regards,\nAdmin Team";
                                emailService.sendSimpleEmailToUserId(company.getUser().getUserId(), subject, body);
                            }
                        });
                    }
                    item.setStatus("RELEASED");
                    anyReleased = true;
                } else {
                    item.setStatus("CREATED");
                    allReleased = false;
                }

                pr.getItems().add(item);
            }
        } else {
            allReleased = false;
        }

        if (anyReleased) {
            if (allReleased) {
                pr.setStatus(PurchaseRequisitionStatus.RELEASED);
            } else {
                pr.setStatus(PurchaseRequisitionStatus.PARTIALLY_RELEASED);
            }
        } else {
            pr.setStatus(PurchaseRequisitionStatus.CREATED);
        }

        pr.setTotalAmount(totalAmount);

        PurchaseRequisition savedPr = prRepository.save(pr);
        return mapToResponse(savedPr);
    }

    @Override
    @Transactional
    public void changePurchaseRequisitionStatus(Long id, PurchaseRequisitionStatusRequest statusRequest) {
        PurchaseRequisition pr = prRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found with id: " + id));

        PurchaseRequisitionStatus newStatus = statusRequest.getStatus();
        PurchaseRequisitionStatus currentStatus = pr.getStatus();

        // Validate Transitions
        if (currentStatus == PurchaseRequisitionStatus.APPROVED) {
            throw new RuntimeException("Approved PR cannot change status");
        }
        if (currentStatus == PurchaseRequisitionStatus.REJECTED) {
            throw new RuntimeException("Rejected PR cannot change status");
        }

        if (currentStatus == PurchaseRequisitionStatus.CREATED && newStatus != PurchaseRequisitionStatus.RELEASED) {
            throw new RuntimeException("Created PR can only be transitioned to RELEASED");
        }

        if (currentStatus == PurchaseRequisitionStatus.RELEASED && (newStatus != PurchaseRequisitionStatus.APPROVED
                && newStatus != PurchaseRequisitionStatus.REJECTED)) {
            throw new RuntimeException("Released PR can only be transitioned to APPROVED or REJECTED");
        }

        pr.setStatus(newStatus);
        prRepository.save(pr);
    }

    @Override
    @Transactional
    public void deletePurchaseRequisition(Long id) {
        PurchaseRequisition pr = prRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found with id: " + id));

        if (pr.getStatus() != PurchaseRequisitionStatus.CREATED) {
            throw new RuntimeException("Only created PR can be deleted");
        }

        prRepository.delete(pr);
    }

    @Override
    public PurchaseRequisitionResponse getPurchaseRequisitionByPrNumber(String prNumber) {
        PurchaseRequisition pr = prRepository.findByPrNumber(prNumber)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found with PR Number: " + prNumber));
        return mapToResponse(pr);
    }

    @Override
    public List<PurchaseRequisitionResponse> getAllVendorPurchaseRequisitions(Long vendorId) {
        List<PurchaseRequisitionItemVendor> assignments = vendorRepository.findByVendorIdWithDetails(vendorId);
        
        String paymentTerms = null;
        String incoterms = null;
        List<FinancialTerms> termsList = financialTermsRepository.findByCompany_CompanyIdAndIsActive(vendorId, true);
        if (termsList != null && !termsList.isEmpty()) {
            FinancialTerms terms = termsList.get(0);
            paymentTerms = terms.getTermsOfPayment();
            incoterms = terms.getDeliveryTerms();
        }

        java.util.Map<PurchaseRequisition, List<PurchaseRequisitionItem>> prItemsMap = assignments.stream()
                .map(PurchaseRequisitionItemVendor::getPurchaseRequisitionItem)
                .distinct()
                .collect(Collectors.groupingBy(PurchaseRequisitionItem::getPurchaseRequisition));
                
        String finalPaymentTerms = paymentTerms;
        String finalIncoterms = incoterms;

        return prItemsMap.entrySet().stream().map(entry -> {
            PurchaseRequisition pr = entry.getKey();
            List<PurchaseRequisitionItem> items = entry.getValue();
            
            PurchaseRequisitionResponse response = mapToResponseWithoutItems(pr);
            response.setTotalAmount(null);
            response.setPaymentTerms(finalPaymentTerms);
            response.setIncoterms(finalIncoterms);
            
            String vendorStatus = assignments.stream()
                    .filter(a -> a.getPurchaseRequisitionItem().getPurchaseRequisition().getId().equals(pr.getId()))
                    .map(PurchaseRequisitionItemVendor::getStatus)
                    .findFirst()
                    .orElse("SENT");
            response.setVendorStatus(vendorStatus);
            
            List<PurchaseRequisitionItemResponse> itemResponses = items.stream().map(item -> {
                PurchaseRequisitionItemResponse itemRes = new PurchaseRequisitionItemResponse();
                itemRes.setId(item.getId());
                itemRes.setMaterialId(item.getMaterialId());
                itemRes.setSku(item.getSku());
                itemRes.setQuantity(item.getQuantity());
                itemRes.setUom(item.getUom());
                itemRes.setEstimatedPrice(null);
                itemRes.setTotalPrice(null);
                itemRes.setRequiredDate(pr.getRequiredDate());
                
                materialRepository.findById(item.getMaterialId()).ifPresent(material -> {
                    itemRes.setMaterialDescription(material.getDescription());
                    itemRes.setHsnSac(material.getHsnCode());
                });

                return itemRes;
            }).collect(Collectors.toList());
            
            response.setItems(itemResponses);
            response.setItemCount(itemResponses.size());
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public PurchaseRequisitionResponse getVendorPurchaseRequisitionByPrNumber(String prNumber, Long vendorId) {
        PurchaseRequisition pr = prRepository.findByPrNumber(prNumber)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found with PR Number: " + prNumber));
        
        PurchaseRequisitionResponse response = mapToResponseWithoutItems(pr);
        response.setTotalAmount(null);
        
        List<PurchaseRequisitionItemVendor> assignments = vendorRepository.findByVendorIdWithDetails(vendorId);
        String vendorStatus = assignments.stream()
                .filter(a -> a.getPurchaseRequisitionItem().getPurchaseRequisition().getId().equals(pr.getId()))
                .map(PurchaseRequisitionItemVendor::getStatus)
                .findFirst()
                .orElse("SENT");
        response.setVendorStatus(vendorStatus);
        
        List<FinancialTerms> termsList = financialTermsRepository.findByCompany_CompanyIdAndIsActive(vendorId, true);
        if (termsList != null && !termsList.isEmpty()) {
            FinancialTerms terms = termsList.get(0);
            response.setPaymentTerms(terms.getTermsOfPayment());
            response.setIncoterms(terms.getDeliveryTerms());
        }
        
        List<PurchaseRequisitionItemResponse> vendorItems = pr.getItems().stream()
                .filter(item -> item.getItemVendors().stream().anyMatch(v -> v.getVendorId().equals(vendorId)))
                .map(item -> {
                    PurchaseRequisitionItemResponse itemRes = new PurchaseRequisitionItemResponse();
                    itemRes.setId(item.getId());
                    itemRes.setMaterialId(item.getMaterialId());
                    itemRes.setSku(item.getSku());
                    itemRes.setQuantity(item.getQuantity());
                    itemRes.setUom(item.getUom());
                    itemRes.setEstimatedPrice(null);
                    itemRes.setTotalPrice(null);
                    itemRes.setRequiredDate(pr.getRequiredDate());
                    
                    materialRepository.findById(item.getMaterialId()).ifPresent(material -> {
                        itemRes.setMaterialDescription(material.getDescription());
                        itemRes.setHsnSac(material.getHsnCode());
                    });
                    
                    return itemRes;
                })
                .collect(Collectors.toList());
                
        if (vendorItems.isEmpty()) {
             throw new RuntimeException("No items assigned to this vendor for PR Number: " + prNumber);
        }
                
        response.setItems(vendorItems);
        response.setItemCount(vendorItems.size());
        
        return response;
    }

    @Override
    public List<VendorPurchaseRequisitionItemResponse> getVendorAssignedItems(Long vendorId) {
        List<PurchaseRequisitionItemVendor> assignments = vendorRepository.findByVendorIdWithDetails(vendorId);
        return assignments.stream().map(a -> {
            VendorPurchaseRequisitionItemResponse res = new VendorPurchaseRequisitionItemResponse();
            res.setAssignmentId(a.getId());
            res.setPrNumber(a.getPurchaseRequisitionItem().getPurchaseRequisition().getPrNumber());
            res.setRequiredDate(a.getPurchaseRequisitionItem().getPurchaseRequisition().getRequiredDate());
            res.setMaterialSku(a.getPurchaseRequisitionItem().getSku());
            res.setQuantity(a.getPurchaseRequisitionItem().getQuantity());
            res.setUom(a.getPurchaseRequisitionItem().getUom());
            res.setAssignmentStatus(a.getStatus());
            res.setSentDate(a.getSentAt());
            return res;
        }).collect(Collectors.toList());
    }

    private String resolveRequestedByName(Long requestedById) {
        if (requestedById == null) {
            return "System";
        }
        
        Optional<UserDetail> userOpt = userDetailRepository.findById(requestedById);
        if (userOpt.isPresent()) {
            UserDetail user = userOpt.get();
            String name = (user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : "");
            return name.trim().isEmpty() ? user.getEmail() : name.trim();
        }
        
        Optional<SuperAdmin> adminOpt = superAdminRepository.findById(requestedById);
        if (adminOpt.isPresent()) {
            SuperAdmin admin = adminOpt.get();
            String name = (admin.getFirstName() != null ? admin.getFirstName() : "") + " " + (admin.getLastName() != null ? admin.getLastName() : "");
            return name.trim().isEmpty() ? admin.getEmail() : name.trim();
        }
        
        return "ID: " + requestedById;
    }

    private PurchaseRequisitionResponse mapToResponse(PurchaseRequisition pr) {
        PurchaseRequisitionResponse response = new PurchaseRequisitionResponse();
        response.setId(pr.getId());
        response.setPrNumber(pr.getPrNumber());
        response.setLocationId(pr.getLocationId());

        if (pr.getLocationId() != null) {
            locationRepository.findById(pr.getLocationId())
                    .ifPresent(location -> response.setLocationName(location.getLocationName()));
        }

        response.setRequestedBy(resolveRequestedByName(pr.getRequestedBy()));
        response.setRequiredDate(pr.getRequiredDate());
        response.setRemarks(pr.getRemarks());
        response.setStatus(pr.getStatus());
        response.setTotalAmount(pr.getTotalAmount());
        response.setCreatedAt(pr.getCreatedAt());
        response.setUpdatedAt(pr.getUpdatedAt());

        if (pr.getItems() != null) {
            List<PurchaseRequisitionItemResponse> itemResponses = pr.getItems().stream()
                    .map(item -> {
                        PurchaseRequisitionItemResponse itemRes = new PurchaseRequisitionItemResponse();
                        itemRes.setId(item.getId());
                        itemRes.setMaterialId(item.getMaterialId());
                        itemRes.setSku(item.getSku());
                        itemRes.setQuantity(item.getQuantity());
                        itemRes.setUom(item.getUom());
                        itemRes.setEstimatedPrice(item.getEstimatedPrice());
                        itemRes.setTotalPrice(item.getTotalPrice());
                        
                        if (item.getItemVendors() != null) {
                            List<Long> vIds = item.getItemVendors().stream()
                                    .map(PurchaseRequisitionItemVendor::getVendorId)
                                    .collect(Collectors.toList());
                            itemRes.setVendorIds(vIds);
                            
                            List<String> vBpNos = item.getItemVendors().stream()
                                    .map(PurchaseRequisitionItemVendor::getBpNo)
                                    .collect(Collectors.toList());
                            itemRes.setVendorBpNos(vBpNos);
                        }
                        
                        return itemRes;
                    }).collect(Collectors.toList());
            response.setItems(itemResponses);
            response.setItemCount(itemResponses.size());
        } else {
            response.setItemCount(0);
        }

        return response;
    }

    private PurchaseRequisitionResponse mapToResponseWithoutItems(PurchaseRequisition pr) {
        PurchaseRequisitionResponse response = new PurchaseRequisitionResponse();
        response.setId(pr.getId());
        response.setPrNumber(pr.getPrNumber());
        response.setLocationId(pr.getLocationId());

        if (pr.getLocationId() != null) {
            locationRepository.findById(pr.getLocationId())
                    .ifPresent(location -> response.setLocationName(location.getLocationName()));
        }

        response.setRequestedBy(resolveRequestedByName(pr.getRequestedBy()));
        response.setRequiredDate(pr.getRequiredDate());
        response.setRemarks(pr.getRemarks());
        response.setStatus(pr.getStatus());
        response.setTotalAmount(pr.getTotalAmount());
        response.setCreatedAt(pr.getCreatedAt());
        response.setUpdatedAt(pr.getUpdatedAt());

        response.setItemCount(pr.getItems() != null ? pr.getItems().size() : 0);

        return response;
    }

    @Override
    @Transactional
    public void respondToPurchaseRequisition(Long prId, Long vendorId, String action) {
        List<PurchaseRequisitionItemVendor> assignments = vendorRepository.findByVendorIdAndPrId(vendorId, prId);
        if (assignments == null || assignments.isEmpty()) {
            throw new IllegalArgumentException("No assigned items found for the vendor and PR ID: " + prId);
        }

        String newStatus;
        if ("ACCEPT".equalsIgnoreCase(action) || "ACCEPTED".equalsIgnoreCase(action)) {
            newStatus = "ACCEPTED";
        } else if ("REJECT".equalsIgnoreCase(action) || "REJECTED".equalsIgnoreCase(action)) {
            newStatus = "REJECTED";
        } else {
            throw new IllegalArgumentException("Invalid action: " + action + ". Allowed values are ACCEPT or REJECT.");
        }

        for (PurchaseRequisitionItemVendor assignment : assignments) {
            assignment.setStatus(newStatus);
        }
        vendorRepository.saveAll(assignments);
    }

    @Override
    public List<PurchaseRequisitionResponse> getAcceptedVendorPurchaseRequisitions(Long vendorId) {
        List<PurchaseRequisitionItemVendor> assignments = vendorRepository.findByVendorIdAndStatus(vendorId, "ACCEPTED");
        
        String paymentTerms = null;
        String incoterms = null;
        List<FinancialTerms> termsList = financialTermsRepository.findByCompany_CompanyIdAndIsActive(vendorId, true);
        if (termsList != null && !termsList.isEmpty()) {
            FinancialTerms terms = termsList.get(0);
            paymentTerms = terms.getTermsOfPayment();
            incoterms = terms.getDeliveryTerms();
        }

        java.util.Map<PurchaseRequisition, List<PurchaseRequisitionItem>> prItemsMap = assignments.stream()
                .map(PurchaseRequisitionItemVendor::getPurchaseRequisitionItem)
                .distinct()
                .collect(Collectors.groupingBy(PurchaseRequisitionItem::getPurchaseRequisition));
                
        String finalPaymentTerms = paymentTerms;
        String finalIncoterms = incoterms;

        return prItemsMap.entrySet().stream().map(entry -> {
            PurchaseRequisition pr = entry.getKey();
            List<PurchaseRequisitionItem> items = entry.getValue();
            
            PurchaseRequisitionResponse response = mapToResponseWithoutItems(pr);
            response.setTotalAmount(null);
            response.setPaymentTerms(finalPaymentTerms);
            response.setIncoterms(finalIncoterms);
            response.setVendorStatus("ACCEPTED");
            
            List<PurchaseRequisitionItemResponse> itemResponses = items.stream().map(item -> {
                PurchaseRequisitionItemResponse itemRes = new PurchaseRequisitionItemResponse();
                itemRes.setId(item.getId());
                itemRes.setMaterialId(item.getMaterialId());
                itemRes.setSku(item.getSku());
                itemRes.setQuantity(item.getQuantity());
                itemRes.setUom(item.getUom());
                itemRes.setEstimatedPrice(null);
                itemRes.setTotalPrice(null);
                itemRes.setRequiredDate(pr.getRequiredDate());
                
                materialRepository.findById(item.getMaterialId()).ifPresent(material -> {
                    itemRes.setMaterialDescription(material.getDescription());
                    itemRes.setHsnSac(material.getHsnCode());
                });

                return itemRes;
            }).collect(Collectors.toList());
            
            response.setItems(itemResponses);
            response.setItemCount(itemResponses.size());
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public PurchaseRequisitionResponse getAcceptedVendorPurchaseRequisitionById(Long prId, Long vendorId) {
        List<PurchaseRequisitionItemVendor> assignments = vendorRepository.findAcceptedByVendorIdAndPrId(vendorId, prId);
        if (assignments == null || assignments.isEmpty()) {
            throw new IllegalArgumentException("No accepted purchase requisition found for ID: " + prId);
        }

        PurchaseRequisition pr = assignments.get(0).getPurchaseRequisitionItem().getPurchaseRequisition();
        List<PurchaseRequisitionItem> items = assignments.stream()
                .map(PurchaseRequisitionItemVendor::getPurchaseRequisitionItem)
                .distinct()
                .collect(Collectors.toList());

        PurchaseRequisitionResponse response = mapToResponseWithoutItems(pr);
        response.setTotalAmount(null);
        response.setVendorStatus("ACCEPTED");
        
        List<FinancialTerms> termsList = financialTermsRepository.findByCompany_CompanyIdAndIsActive(vendorId, true);
        if (termsList != null && !termsList.isEmpty()) {
            FinancialTerms terms = termsList.get(0);
            response.setPaymentTerms(terms.getTermsOfPayment());
            response.setIncoterms(terms.getDeliveryTerms());
        }
        
        List<PurchaseRequisitionItemResponse> itemResponses = items.stream().map(item -> {
            PurchaseRequisitionItemResponse itemRes = new PurchaseRequisitionItemResponse();
            itemRes.setId(item.getId());
            itemRes.setMaterialId(item.getMaterialId());
            itemRes.setSku(item.getSku());
            itemRes.setQuantity(item.getQuantity());
            itemRes.setUom(item.getUom());
            itemRes.setEstimatedPrice(null);
            itemRes.setTotalPrice(null);
            itemRes.setRequiredDate(pr.getRequiredDate());
            
            materialRepository.findById(item.getMaterialId()).ifPresent(material -> {
                itemRes.setMaterialDescription(material.getDescription());
                itemRes.setHsnSac(material.getHsnCode());
            });

            return itemRes;
        }).collect(Collectors.toList());
        
        response.setItems(itemResponses);
        response.setItemCount(itemResponses.size());
        
        return response;
    }
}
