package com.example.multimedia.file_upload_api.dto.materialinward;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class MaterialInwardDetailDto {
    private Long gateEntryId;
    private String gateEntryNo;
    private String gateIn;
    private String vehicleNo;
    
    private String vendorName;
    private String vendorCode;
    
    private String packingSlipNo;
    private String packingSlipDate;
    
    private String poReference;
    private String poDate;
    
    private String invoiceNo;
    private String invoiceDate;
    
    private String destination;
    
    private List<BoxDto> boxes;
    
    @Data
    public static class BoxDto {
        private String id;
        private String boxNo;
        private String manifestSeal;
        private String weight;
        private List<LineDto> lines;
    }
    
    @Data
    public static class LineDto {
        private String id;
        private String itemNo;
        private String description;
        private String uom;
        private Double manifestQty;
        private String bin;
        private List<BatchDto> batches; // Optional, if batched
    }
    
    @Data
    public static class BatchDto {
        private String batchNo;
        private Double qty;
    }
}
