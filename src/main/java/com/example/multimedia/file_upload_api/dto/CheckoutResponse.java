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
public class CheckoutResponse {
    
    private String orderNumber;
    private String orderId;
    private String status;
    private String message;
    private OrderDetails orderDetails;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetails {
        private CustomerDetails customerInfo;
        private List<OrderItemDetails> orderItems;
        private OrderSummaryDetails orderSummary;
        private OrderMetadataDetails orderMetadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDetails {
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
    public static class OrderItemDetails {
        private Long materialId;
        private String materialName;
        private String materialCode;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalPrice;
        private Long channelId;
        private String imageName;
        private String imageType;
        private LocalDateTime addedAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryDetails {
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
    public static class OrderMetadataDetails {
        private LocalDateTime orderDate;
        private Long channelId;
        private Long companyId;
        private String orderStatus;
    }
}
