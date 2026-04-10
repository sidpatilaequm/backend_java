package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionItemRequest;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionItemResponse;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionRequest;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionResponse;
import com.example.multimedia.file_upload_api.dto.PurchaseRequisitionStatusRequest;
import com.example.multimedia.file_upload_api.entity.PurchaseRequisition;
import com.example.multimedia.file_upload_api.entity.PurchaseRequisitionItem;
import com.example.multimedia.file_upload_api.entity.Location;
import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import com.example.multimedia.file_upload_api.repository.PurchaseRequisitionRepository;
import com.example.multimedia.file_upload_api.repository.LocationRepository;
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
import java.util.stream.Collectors;

@Service
public class PurchaseRequisitionServiceImpl implements PurchaseRequisitionService {

    @Autowired
    private PurchaseRequisitionRepository prRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    @Transactional
    public PurchaseRequisitionResponse createPurchaseRequisition(PurchaseRequisitionRequest request) {
        PurchaseRequisition pr = new PurchaseRequisition();
        pr.setLocationId(request.getLocationId());
        pr.setRequiredDate(request.getRequiredDate());
        pr.setRemarks(request.getRemarks());
        pr.setStatus(request.getStatus() != null ? request.getStatus() : PurchaseRequisitionStatus.DRAFT);

        Long superAdminId = currentUserService.getCurrentSuperAdminId();
        pr.setRequestedBy(superAdminId);

        String nextPrNumber = generateNextPrNumber();
        pr.setPrNumber(nextPrNumber);

        BigDecimal totalAmount = BigDecimal.ZERO;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PurchaseRequisitionItemRequest itemReq : request.getItems()) {
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

                pr.getItems().add(item);
            }
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
        Long superAdminId = currentUserService.getCurrentSuperAdminId();
        Page<PurchaseRequisition> prPage = prRepository.findWithFilters(superAdminId, locationId, status, search,
                pageable);
        return prPage.map(this::mapToResponseWithoutItems);
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
        PurchaseRequisition pr = prRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found with id: " + id));

        if (pr.getStatus() != PurchaseRequisitionStatus.DRAFT) {
            throw new RuntimeException("Only draft PR can be edited");
        }

        pr.setLocationId(request.getLocationId());
        pr.setRequiredDate(request.getRequiredDate());
        pr.setRemarks(request.getRemarks());

        pr.getItems().clear();

        BigDecimal totalAmount = BigDecimal.ZERO;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PurchaseRequisitionItemRequest itemReq : request.getItems()) {
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

                pr.getItems().add(item);
            }
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

        if (currentStatus == PurchaseRequisitionStatus.DRAFT && newStatus != PurchaseRequisitionStatus.SUBMITTED) {
            throw new RuntimeException("Draft PR can only be transitioned to SUBMITTED");
        }

        if (currentStatus == PurchaseRequisitionStatus.SUBMITTED && (newStatus != PurchaseRequisitionStatus.APPROVED
                && newStatus != PurchaseRequisitionStatus.REJECTED)) {
            throw new RuntimeException("Submitted PR can only be transitioned to APPROVED or REJECTED");
        }

        pr.setStatus(newStatus);
        prRepository.save(pr);
    }

    @Override
    @Transactional
    public void deletePurchaseRequisition(Long id) {
        PurchaseRequisition pr = prRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found with id: " + id));

        if (pr.getStatus() != PurchaseRequisitionStatus.DRAFT) {
            throw new RuntimeException("Only draft PR can be deleted");
        }

        prRepository.delete(pr);
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

        response.setRequestedBy(pr.getRequestedBy());
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

        response.setRequestedBy(pr.getRequestedBy());
        response.setRequiredDate(pr.getRequiredDate());
        response.setRemarks(pr.getRemarks());
        response.setStatus(pr.getStatus());
        response.setTotalAmount(pr.getTotalAmount());
        response.setCreatedAt(pr.getCreatedAt());
        response.setUpdatedAt(pr.getUpdatedAt());

        response.setItemCount(pr.getItems() != null ? pr.getItems().size() : 0);

        return response;
    }
}
