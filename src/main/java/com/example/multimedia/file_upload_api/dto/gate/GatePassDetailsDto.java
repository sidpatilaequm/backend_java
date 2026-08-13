package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GatePassDetailsDto {
    private String gatePassNumber;
    private LocalDateTime inTime;
    private Integer countedPackages;
    private List<LineDto> lines;

    @Data
    public static class LineDto {
        private String materialCode;
        private BigDecimal declaredQty;
        private BigDecimal countedQty;
        private String uom;
    }
}
