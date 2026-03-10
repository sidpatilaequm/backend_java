package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveFromCartResponseDTO {
    
    private Long removedItemId;
    private Integer remainingItems;
    private BigDecimal newTotalPrice;
}
