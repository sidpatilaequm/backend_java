package com.example.multimedia.file_upload_api.dto;

import java.math.BigDecimal;

public class PurchaseRequisitionItemResponse {
    private Long id;
    private Long materialId;
    private String sku;
    private BigDecimal quantity;
    private String uom;
    private BigDecimal estimatedPrice;
    private BigDecimal totalPrice;
    private String materialDescription;
    private String hsnSac;
    private java.time.LocalDate requiredDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getMaterialDescription() {
        return materialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        this.materialDescription = materialDescription;
    }

    public String getHsnSac() {
        return hsnSac;
    }

    public void setHsnSac(String hsnSac) {
        this.hsnSac = hsnSac;
    }

    public java.time.LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(java.time.LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    private java.util.List<Long> vendorIds;
    private java.util.List<String> vendorBpNos;

    public java.util.List<Long> getVendorIds() {
        return vendorIds;
    }

    public void setVendorIds(java.util.List<Long> vendorIds) {
        this.vendorIds = vendorIds;
    }

    public java.util.List<String> getVendorBpNos() {
        return vendorBpNos;
    }

    public void setVendorBpNos(java.util.List<String> vendorBpNos) {
        this.vendorBpNos = vendorBpNos;
    }
}
