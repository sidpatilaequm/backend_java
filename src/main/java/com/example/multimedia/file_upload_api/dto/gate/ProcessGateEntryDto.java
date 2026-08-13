package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProcessGateEntryDto {
    private String asnNumber;
    private String decision; // ALLOW, HOLD, REJECT
    private Map<String, String> documents;
    private PackageVerificationDto packageVerification;
    private List<LineVerificationDto> lineVerification;

    @Data
    public static class PackageVerificationDto {
        private Integer counted;
        private String remark;
    }

    @Data
    public static class LineVerificationDto {
        private String materialCode;
        private BigDecimal countedQty;
        private String remark;
    }
}
