package com.example.multimedia.file_upload_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AsnRequestDto {

    @JsonProperty("po_id")
    private String poId;

    @JsonProperty("vendor_bpno")
    private String vendorBpno;

    @JsonProperty("shipment_details")
    private ShipmentDetailsDto shipmentDetails;

    @JsonProperty("items")
    private List<AsnItemRequestDto> items;

    @JsonProperty("packages")
    private List<AsnPackageDto> packages;
}
