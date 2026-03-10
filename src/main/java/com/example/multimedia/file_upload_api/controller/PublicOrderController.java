package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.CheckoutRequest;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/public/orders")
public class PublicOrderController {

    private static final Logger logger = LoggerFactory.getLogger(PublicOrderController.class);

    @Autowired
    private OrderService orderService;

    /**
     * Public checkout endpoint - no authentication required
     * POST /api/public/orders/checkout
     */
    @PostMapping("/checkout")
    public ResponseEntity<ServiceResponse> checkout(
            @RequestBody CheckoutRequest request,
            HttpServletRequest httpRequest) {
        
        try {

            logger.info("Processing checkout for company: {}, channel: {}", 
                request.getOrderMetadata() != null ? request.getOrderMetadata().getCompanyId() : "unknown",
                request.getOrderMetadata() != null ? request.getOrderMetadata().getChannelId() : "unknown");
            
            ServiceResponse response = orderService.processCheckout(request);
            
            if ("SUCCESS".equals(response.getStatus())) {
                logger.info("Order created successfully with order number: {}", 
                    response.getData().get("orderNumber"));
                return ResponseEntity.ok(response);
            } else {
                logger.error("Checkout failed: {}", response.getStatusMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error in checkout endpoint: ", e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Internal server error");
            errorResponse.setErrorCode("INTERNAL_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get order by order number - public endpoint
     * GET /api/public/orders/{orderNumber}
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<ServiceResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber) {
        
        try {
            logger.info("Retrieving order with order number: {}", orderNumber);
            ServiceResponse response = orderService.getOrderByOrderNumber(orderNumber);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error in getOrderByOrderNumber endpoint: ", e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Internal server error");
            errorResponse.setErrorCode("INTERNAL_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}
