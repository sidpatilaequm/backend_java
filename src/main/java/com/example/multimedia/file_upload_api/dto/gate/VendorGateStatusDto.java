package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VendorGateStatusDto {
    private String asnNumber;
    private String vehicleNo;
    private String gateStatus;
    private String gatePassNumber;
    private LocalDateTime processedAt;
}
