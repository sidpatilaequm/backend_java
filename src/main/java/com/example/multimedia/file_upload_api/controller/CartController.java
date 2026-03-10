package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.*;
import com.example.multimedia.file_upload_api.service.CartService;
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
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Add item to cart
     * POST /api/cart/add-item
     */
    @PostMapping("/add-item")
    public ResponseEntity<ServiceResponse> addItemToCart(
            @RequestBody AddToCartRequest request,
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

            logger.info("Adding item to cart for company: {}, material: {}", companyId, request.getMaterialId());
            ServiceResponse response = cartService.addItemToCart(request, companyId);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error in addItemToCart endpoint: ", e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Internal server error");
            errorResponse.setErrorCode("INTERNAL_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get cart items with optional filters
     * GET /api/cart/items?channelId=3
     */
    @GetMapping("/items")
    public ResponseEntity<ServiceResponse> getCartItems(
            @RequestParam(required = false) Long channelId,
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

            logger.info("Getting cart items for company: {}, channelId: {}", companyId, channelId);
            ServiceResponse response = cartService.getCartItems(companyId, channelId);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error in getCartItems endpoint: ", e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Internal server error");
            errorResponse.setErrorCode("INTERNAL_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Remove item from cart
     * DELETE /api/cart/remove-item?cartItemId=123
     */
    @DeleteMapping("/remove-item")
    public ResponseEntity<ServiceResponse> removeItemFromCart(
            @RequestParam Long cartItemId,
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

            logger.info("Removing cart item: {} for company: {}", cartItemId, companyId);
            ServiceResponse response = cartService.removeItemFromCart(cartItemId, companyId);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error in removeItemFromCart endpoint: ", e);
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Internal server error");
            errorResponse.setErrorCode("INTERNAL_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Clear entire cart
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ServiceResponse> clearCart(HttpServletRequest httpRequest) {
        
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

            logger.info("Clearing cart for company: {}", companyId);
            ServiceResponse response = cartService.clearCart(companyId);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error in clearCart endpoint: ", e);
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
