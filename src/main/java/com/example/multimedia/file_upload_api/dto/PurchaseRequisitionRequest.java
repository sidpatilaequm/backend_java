package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import java.time.LocalDate;
import java.util.List;

public class PurchaseRequisitionRequest {
    private Long locationId;
    private LocalDate requiredDate;
    private String remarks;
    private PurchaseRequisitionStatus status = PurchaseRequisitionStatus.DRAFT;
    private List<PurchaseRequisitionItemRequest> items;

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public PurchaseRequisitionStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseRequisitionStatus status) {
        this.status = status;
    }

    public List<PurchaseRequisitionItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PurchaseRequisitionItemRequest> items) {
        this.items = items;
    }
}
