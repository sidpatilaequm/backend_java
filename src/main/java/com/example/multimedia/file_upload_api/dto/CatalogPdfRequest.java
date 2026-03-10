package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class CatalogPdfRequest {
    
    // Catalog header information
    private String channelName;
    private String channelCode;
    private String companyName;
    private String catalogTitle;
    private String catalogDescription;
    
    // Styling options
    private String primaryColor;
    private String secondaryColor;
    private String logoUrl;
    
    // Grid layout configuration
    private Integer productsPerRow = 3; // Default: 3 products per row (2, 3, or 4)
    
    // Product cards data
    private List<ProductCard> products;
    
                    @Data
                @NoArgsConstructor
                public static class ProductCard {
                    private String productName;
                    private String sku;
                    private String category;
                    private BigDecimal price;
                    private String materialId;        // Database material ID
                    private String imageName;       // Image filename from database
                    private String description;
                    private String size;
                    private String color;
                    private String specifications;
                    private String productUrl;       // Product detail page URL
                }
}
