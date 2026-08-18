package com.example.multimedia.file_upload_api.dto;

import java.util.List;

public class CreateRfqRequest {
    private List<Long> vendor_ids;

    public List<Long> getVendor_ids() {
        return vendor_ids;
    }

    public void setVendor_ids(List<Long> vendor_ids) {
        this.vendor_ids = vendor_ids;
    }
}
