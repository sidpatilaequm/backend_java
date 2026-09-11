package com.example.multimedia.file_upload_api.dto.materialinward;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GoodsReceiptResponseDto {
    private Long id;
    private Long gateEntryId;
    private String gatePassNumber;
    private Long asnId;
    private String decision;
    private String grnNumber;
    private String rtvNumber;
    private String processedBy;
    private String remarks;
    private LocalDateTime createdDate;
}
