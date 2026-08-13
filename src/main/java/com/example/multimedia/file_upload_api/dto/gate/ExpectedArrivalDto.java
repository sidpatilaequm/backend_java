package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExpectedArrivalDto {
    private String asnNumber;
    private String poNumber;
    private String vendorName;
    private String vehicleNo;
    private LocalDateTime eta;
    private String etaTag;
    private Integer totalPackages;
}
