package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AsnHistoryResponseDto {
    private Long asnId;
    private String asnNumber;
    private LocalDate dispatchDate;
    private String status;
    private String effect;
    private String grnNumber;
    private String invoiceNumber;
    private String ewayBill;
    private String vehicleNumber;
    private String eta;
    private String remarks;
    private List<DocumentDto> documents;
    private List<LineDto> lines;

    @Data
    public static class DocumentDto {
        private String name;
        private String url;
    }

    @Data
    public static class LineDto {
        private Integer lineNumber;
        private String partNumber;
        private BigDecimal quantity;
        private BigDecimal grnQuantity;
    }
}
