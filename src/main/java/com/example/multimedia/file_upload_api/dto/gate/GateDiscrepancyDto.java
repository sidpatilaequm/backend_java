package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GateDiscrepancyDto {
    private String asnNumber;
    private String gateStatus;
    private DiscrepancyDetailsDto discrepancies;

    @Data
    public static class DiscrepancyDetailsDto {
        private List<String> documents;
        private String packages;
        private List<LineDiscrepancyDto> lines;
    }

    @Data
    public static class LineDiscrepancyDto {
        private String materialCode;
        private BigDecimal declared;
        private BigDecimal counted;
        private String remark;
    }
}
