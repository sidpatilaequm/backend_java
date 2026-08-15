package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AsnResponseDto {
    private Long id;
    private String poNumber;
    private String vendorBpno;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String ewayBill;
    private LocalDate ewbValidTo;
    private String vehicleNumber;
    private String transporterCode;
    private LocalDate dispatchDate;
    private LocalDate expectedDelivery;
    private String packaging;
    private Integer noOfPackages;
    private String status;
    private String taxInvoiceUrl;
    private String ewayBillUrl;
    private String packingListUrl;
    private String pdirUrl;
    private String deviationUrl;
    private String othersUrl;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private boolean isPartial;
    
    private List<AsnItemResponseDto> items;
}
