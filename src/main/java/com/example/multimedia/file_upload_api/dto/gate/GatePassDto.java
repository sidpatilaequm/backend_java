package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GatePassDto {
    private String gatePassNumber;
    private String asnNumber;
    private String vehicleNo;
    private LocalDateTime inTime;
    private String processedBy;
}
