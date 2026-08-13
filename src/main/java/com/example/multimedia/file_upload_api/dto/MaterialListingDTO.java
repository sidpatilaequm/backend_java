package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.enums.ListingStatus;
import com.example.multimedia.file_upload_api.enums.SyncStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MaterialListingDTO {
    private Long id;
    private Long materialId;
    private Long channelId;
    private Long channelCategoryId;
    private Long companyId;
    private String channelSku;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private Integer availableStock;
    private ListingStatus listingStatus;
    private SyncStatus syncStatus;
    private String validationStatus;
}
