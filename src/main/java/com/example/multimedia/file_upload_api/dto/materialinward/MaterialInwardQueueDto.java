package com.example.multimedia.file_upload_api.dto.materialinward;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialInwardQueueDto {
    private Long gateEntryId;
    private String gateEntryNo;
    private String vehicleNo;
    private LocalDateTime gateInTime;
    
    // Vendor and PO info
    private String vendorName;
    private String vendorCode;
    private String poReference;
    private String packingSlipNo;
    private String status;
    private Integer noOfBoxes;
}
