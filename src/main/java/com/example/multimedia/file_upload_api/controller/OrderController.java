package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.OrderService;
import com.example.multimedia.file_upload_api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get orders for authenticated company
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<ServiceResponse> getOrdersByCompany(HttpServletRequest httpRequest) {
        
        try {
            // Extract company ID from JWT token
            Long companyId = getCompanyIdFromToken(httpRequest);
            if (companyId == null) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setStatus("ERROR");
                errorResponse.setStatusMsg("Unauthorized access");
                errorResponse.setErrorCode("UNAUTHORIZED");
                return ResponseEntity.status(401).body(errorResponse);
            }

            logger.info("Getting orders for company: {}", companyId);
            ServiceResponse response = orderService.getOrdersByCompany(companyId);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error in getOrdersByCompany endpoint: ", e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Internal server error");
            errorResponse.setErrorCode("INTERNAL_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get order by order number for authenticated company
     * GET /api/orders/{orderNumber}
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<ServiceResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            HttpServletRequest httpRequest) {
        
        try {
            // Extract company ID from JWT token
            Long companyId = getCompanyIdFromToken(httpRequest);
            if (companyId == null) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setStatus("ERROR");
                errorResponse.setStatusMsg("Unauthorized access");
                errorResponse.setErrorCode("UNAUTHORIZED");
                return ResponseEntity.status(401).body(errorResponse);
            }

            logger.info("Getting order {} for company: {}", orderNumber, companyId);
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

    /**
     * Extract company ID from JWT token
     */
    private Long getCompanyIdFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // Extract username from token (for logging purposes)
                jwtUtil.extractUsername(token);
                
                // Get company ID from JWT token
                // You might need to adjust this based on your JWT implementation
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    // Extract company ID from authentication context
                    // This is a simplified approach - you might need to customize based on your setup
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                        // You'll need to implement a way to get company ID from UserDetails
                        // For now, returning a placeholder - adjust based on your implementation
                        return 1L; // This should be replaced with actual company ID extraction
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting company ID from token: ", e);
        }
        return null;
    }
}
