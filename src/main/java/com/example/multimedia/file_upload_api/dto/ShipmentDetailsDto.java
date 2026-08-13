package com.example.multimedia.file_upload_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShipmentDetailsDto {

    @JsonProperty("invoice_number")
    private String invoiceNumber;

    @JsonProperty("irn")
    private String irn;

    @JsonProperty("eway_bill")
    private String ewayBill;

    @JsonProperty("ewb_valid_to")
    private LocalDate ewbValidTo;

    @JsonProperty("vehicle_number")
    private String vehicleNumber;

    @JsonProperty("transporter_code")
    private String transporterCode;

    @JsonProperty("lr_number")
    private String lrNumber;

    @JsonProperty("dispatch_date")
    private LocalDate dispatchDate;

    @JsonProperty("expected_delivery")
    private LocalDate expectedDelivery;

    @JsonProperty("packaging")
    private String packaging;
}
