package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    private CustomerInfo customerInfo;
    private List<OrderItemRequest> orderItems;
    private OrderSummary orderSummary;
    private OrderMetadata orderMetadata;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String notes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private Long materialId;
        private String materialName;
        private String materialCode;
        private BigDecimal price;
        private Integer quantity;
        private Long channelId;
        private String imageBase64;
        private String imageType;
        private String imageName;
        private LocalDateTime addedAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private Integer totalItems;
        private BigDecimal subtotal;
        private BigDecimal shipping;
        private BigDecimal tax;
        private BigDecimal discount;
        private BigDecimal total;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderMetadata {
        private LocalDateTime orderDate;
        private String orderId;
        private Long channelId;
        private Long companyId;
    }
}
