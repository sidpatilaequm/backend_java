package com.example.multimedia.file_upload_api.dto.gate;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ArrivalDetailsDto {
    private String asnNumber;
    private String poNumber;
    private VendorDto vendor;
    private InvoiceDto invoice;
    private LogisticsDto logistics;
    private Integer declaredPackages;
    private List<LineDto> lines;

    @Data
    public static class VendorDto {
        private String name;
        private String gstin;
    }

    @Data
    public static class InvoiceDto {
        private String number;
        private String date;
        private BigDecimal value;
    }

    @Data
    public static class LogisticsDto {
        private String ewb;
        private String vehicle;
        private String driver;
    }

    @Data
    public static class LineDto {
        private String materialCode;
        private String description;
        private BigDecimal qty;
        private String uom;
    }
}
