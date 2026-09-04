package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import java.time.LocalDate;
import java.util.List;

public class PurchaseRequisitionRequest {
    private String plantCode;
    private String slocId;
    private LocalDate requiredDate;
    private String remarks;
    private String companyCode;
    private PurchaseRequisitionStatus status = PurchaseRequisitionStatus.CREATED;
    private List<PurchaseRequisitionItemRequest> items;

    public String getPlantCode() {
        return plantCode;
    }

    public void setPlantCode(String plantCode) {
        this.plantCode = plantCode;
    }

    public String getSlocId() {
        return slocId;
    }

    public void setSlocId(String slocId) {
        this.slocId = slocId;
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

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
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
