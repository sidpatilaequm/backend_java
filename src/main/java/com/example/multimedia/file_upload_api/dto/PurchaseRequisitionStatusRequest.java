package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.enums.PurchaseRequisitionStatus;

public class PurchaseRequisitionStatusRequest {
    private PurchaseRequisitionStatus status;

    public PurchaseRequisitionStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseRequisitionStatus status) {
        this.status = status;
    }
}
