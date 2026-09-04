package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class PurchaseRequisitionResponse {
    private Long id;
    private String prNumber;
    private String plantCode;
    private String slocId;
    private String storageLocationLabel;
    private String docTypeCode;
    private String docTypeDescription;
    private Boolean rawMaterial;
    private String requestedBy;
    private LocalDate requiredDate;
    private String remarks;
    private PurchaseRequisitionStatus status;
    private BigDecimal totalAmount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer itemCount;
    private String paymentTerms;
    private String incoterms;
    private List<PurchaseRequisitionItemResponse> items;
    private String vendorStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrNumber() {
        return prNumber;
    }

    public void setPrNumber(String prNumber) {
        this.prNumber = prNumber;
    }

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

    public String getStorageLocationLabel() {
        return storageLocationLabel;
    }

    public void setStorageLocationLabel(String storageLocationLabel) {
        this.storageLocationLabel = storageLocationLabel;
    }

    public String getDocTypeCode() {
        return docTypeCode;
    }

    public void setDocTypeCode(String docTypeCode) {
        this.docTypeCode = docTypeCode;
    }

    public String getDocTypeDescription() {
        return docTypeDescription;
    }

    public void setDocTypeDescription(String docTypeDescription) {
        this.docTypeDescription = docTypeDescription;
    }

    public Boolean getRawMaterial() {
        return rawMaterial;
    }

    public void setRawMaterial(Boolean rawMaterial) {
        this.rawMaterial = rawMaterial;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public List<PurchaseRequisitionItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PurchaseRequisitionItemResponse> items) {
        this.items = items;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getIncoterms() {
        return incoterms;
    }

    public void setIncoterms(String incoterms) {
        this.incoterms = incoterms;
    }

    public String getVendorStatus() {
        return vendorStatus;
    }

    public void setVendorStatus(String vendorStatus) {
        this.vendorStatus = vendorStatus;
    }
}
