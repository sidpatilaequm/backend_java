package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.*;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;



    /**
     * Add item to cart
     */
    public ServiceResponse addItemToCart(AddToCartRequest request, Long companyId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Validate input
            if (request == null || request.getMaterialId() == null || request.getQuantity() == null || 
                request.getQuantity() <= 0) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid request data");
                response.setErrorCode("INVALID_REQUEST");
                return response;
            }

            // Get company
            Optional<CompanyDetails> companyOpt = companyDetailsRepository.findById(companyId);
            if (companyOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Company not found");
                response.setErrorCode("COMPANY_NOT_FOUND");
                return response;
            }

            // Get material
            Optional<Material> materialOpt = materialRepository.findById(request.getMaterialId());
            if (materialOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Material not found");
                response.setErrorCode("MATERIAL_NOT_FOUND");
                return response;
            }

            // Get channel
            Optional<Channel> channelOpt = channelRepository.findById(request.getChannelId());
            if (channelOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Channel not found");
                response.setErrorCode("CHANNEL_NOT_FOUND");
                return response;
            }

            Material material = materialOpt.get();
            Channel channel = channelOpt.get();
            CompanyDetails company = companyOpt.get();

            // Check if item already exists in cart
            Optional<Cart> existingCartOpt = cartRepository.findByCompany_CompanyIdAndMaterial_MaterialIdAndChannel_ChannelId(
                companyId, request.getMaterialId(), request.getChannelId());

            Cart cartItem;
            if (existingCartOpt.isPresent()) {
                // Update quantity if item already exists
                cartItem = existingCartOpt.get();
                cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
                cartItem.setUpdatedAt(LocalDateTime.now());
            } else {
                // Create new cart item
                cartItem = new Cart();
                cartItem.setMaterial(material);
                cartItem.setMaterialName(request.getMaterialName() != null ? request.getMaterialName() : material.getMaterialName());
                cartItem.setMaterialCode(request.getMaterialCode() != null ? request.getMaterialCode() : material.getMaterialCode());
                cartItem.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO);
                cartItem.setQuantity(request.getQuantity());
                cartItem.setChannel(channel);
                cartItem.setChannelCode(request.getChannelCode() != null ? request.getChannelCode() : channel.getChannelCode());
                cartItem.setChannelName(channel.getChannelName());
                cartItem.setCompany(company);
                cartItem.setCompanyName(company.getCompanyName());
                cartItem.setImageId(request.getImageId());
                cartItem.setImageName(request.getImageName());
                cartItem.setImageType(request.getImageType());
                cartItem.setImageBase64(request.getImageBase64());
                cartItem.setAddedAt(LocalDateTime.now());
            }

            // Calculate total price
            cartItem.calculateTotalPrice();
            cartRepository.save(cartItem);

            response.setStatus("SUCCESS");
            response.setStatusMsg("Item added to cart successfully");
            response.addData("cartItemId", cartItem.getCartId());
            response.addData("quantity", cartItem.getQuantity());
            response.addData("totalPrice", cartItem.getTotalPrice());

        } catch (Exception e) {
            logger.error("Error adding item to cart: ", e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to add item to cart");
            response.setErrorCode("ADD_TO_CART_ERROR");
        }

        return response;
    }

    /**
     * Get cart items with optional filters
     */
    public ServiceResponse getCartItems(Long companyId, Long channelId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get cart items with filters
            List<Cart> cartItems = cartRepository.findCartItemsWithFilters(companyId, channelId);

            // Convert to DTOs
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                .map(this::convertToCartItemDTO)
                .collect(Collectors.toList());

            // Create cart summary
            CartSummaryDTO cartSummary = createCartSummary(cartItems);

            // Create response
            CartResponseDTO cartResponse = new CartResponseDTO();
            cartResponse.setCartItems(cartItemDTOs);
            cartResponse.setCartSummary(cartSummary);

            response.setStatus("SUCCESS");
            response.setStatusMsg("Cart items retrieved successfully");
            response.addData("cartItems", cartResponse.getCartItems());
            response.addData("cartSummary", cartResponse.getCartSummary());

        } catch (Exception e) {
            logger.error("Error retrieving cart items: ", e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve cart items");
            response.setErrorCode("GET_CART_ERROR");
        }

        return response;
    }

    /**
     * Remove item from cart
     */
    public ServiceResponse removeItemFromCart(Long cartItemId, Long companyId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Find cart item
            Optional<Cart> cartItemOpt = cartRepository.findById(cartItemId);
            if (cartItemOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Cart item not found");
                response.setErrorCode("CART_ITEM_NOT_FOUND");
                return response;
            }

            Cart cartItem = cartItemOpt.get();
            
            // Verify ownership
            if (!cartItem.getCompany().getCompanyId().equals(companyId)) {
                response.setStatus("ERROR");
                response.setStatusMsg("Unauthorized access to cart item");
                response.setErrorCode("UNAUTHORIZED");
                return response;
            }

            // Remove item
            cartRepository.delete(cartItem);

            // Get remaining items count and total
            List<Cart> remainingItems = cartRepository.findByCompany_CompanyId(companyId);
            Integer remainingCount = remainingItems.size();
            BigDecimal newTotalPrice = remainingItems.stream()
                .map(Cart::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            RemoveFromCartResponseDTO removeResponse = new RemoveFromCartResponseDTO();
            removeResponse.setRemovedItemId(cartItemId);
            removeResponse.setRemainingItems(remainingCount);
            removeResponse.setNewTotalPrice(newTotalPrice);

            response.setStatus("SUCCESS");
            response.setStatusMsg("Item removed from cart successfully");
            response.addData("removedItemId", removeResponse.getRemovedItemId());
            response.addData("remainingItems", removeResponse.getRemainingItems());
            response.addData("newTotalPrice", removeResponse.getNewTotalPrice());

        } catch (Exception e) {
            logger.error("Error removing item from cart: ", e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to remove item from cart");
            response.setErrorCode("REMOVE_FROM_CART_ERROR");
        }

        return response;
    }

    /**
     * Clear entire cart
     */
    public ServiceResponse clearCart(Long companyId) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get cart items before clearing
            List<Cart> cartItems = cartRepository.findByCompany_CompanyId(companyId);
            
            if (cartItems.isEmpty()) {
                response.setStatus("SUCCESS");
                response.setStatusMsg("Cart is already empty");
                response.addData("clearedItems", 0);
                response.addData("clearedChannels", new ArrayList<>());
                response.addData("clearedCompanies", new ArrayList<>());
                return response;
            }

            // Get unique channels and companies
            Set<String> channels = cartItems.stream()
                .map(cart -> cart.getChannelCode())
                .collect(Collectors.toSet());
            
            Set<String> companies = cartItems.stream()
                .map(cart -> cart.getCompanyName())
                .collect(Collectors.toSet());

            // Clear cart
            cartRepository.deleteByCompany_CompanyId(companyId);

            ClearCartResponseDTO clearResponse = new ClearCartResponseDTO();
            clearResponse.setClearedItems(cartItems.size());
            clearResponse.setClearedChannels(new ArrayList<>(channels));
            clearResponse.setClearedCompanies(new ArrayList<>(companies));

            response.setStatus("SUCCESS");
            response.setStatusMsg("Cart cleared successfully");
            response.addData("clearedItems", clearResponse.getClearedItems());
            response.addData("clearedChannels", clearResponse.getClearedChannels());
            response.addData("clearedCompanies", clearResponse.getClearedCompanies());

        } catch (Exception e) {
            logger.error("Error clearing cart: ", e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to clear cart");
            response.setErrorCode("CLEAR_CART_ERROR");
        }

        return response;
    }

    /**
     * Convert Cart entity to CartItemDTO
     */
    private CartItemDTO convertToCartItemDTO(Cart cart) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(cart.getCartId());
        dto.setMaterialId(cart.getMaterial().getMaterialId());
        dto.setMaterialName(cart.getMaterialName());
        dto.setMaterialCode(cart.getMaterialCode());
        dto.setPrice(cart.getPrice());
        dto.setQuantity(cart.getQuantity());
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setChannelId(cart.getChannel().getChannelId());
        dto.setChannelCode(cart.getChannelCode());
        dto.setChannelName(cart.getChannelName());
        dto.setCompanyId(cart.getCompany().getCompanyId());
        dto.setCompanyName(cart.getCompanyName());
        dto.setAddedAt(cart.getAddedAt());
        dto.setImageId(cart.getImageId());
        dto.setImageName(cart.getImageName());
        dto.setImageType(cart.getImageType());
        dto.setImageBase64(cart.getImageBase64());
        return dto;
    }

    /**
     * Create cart summary from cart items
     */
    private CartSummaryDTO createCartSummary(List<Cart> cartItems) {
        CartSummaryDTO summary = new CartSummaryDTO();
        
        // Calculate totals
        Integer totalItems = cartItems.stream()
            .mapToInt(Cart::getQuantity)
            .sum();
        
        BigDecimal totalPrice = cartItems.stream()
            .map(Cart::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by channel
        Map<Long, List<Cart>> channelGroups = cartItems.stream()
            .collect(Collectors.groupingBy(cart -> cart.getChannel().getChannelId()));

        List<CartSummaryDTO.ChannelSummaryDTO> channelSummaries = channelGroups.entrySet().stream()
            .map(entry -> {
                List<Cart> channelItems = entry.getValue();
                Cart firstItem = channelItems.get(0);
                
                CartSummaryDTO.ChannelSummaryDTO channelSummary = new CartSummaryDTO.ChannelSummaryDTO();
                channelSummary.setChannelId(firstItem.getChannel().getChannelId());
                channelSummary.setChannelCode(firstItem.getChannelCode());
                channelSummary.setChannelName(firstItem.getChannelName());
                channelSummary.setItemCount(channelItems.stream().mapToInt(Cart::getQuantity).sum());
                channelSummary.setChannelTotal(channelItems.stream()
                    .map(Cart::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
                
                return channelSummary;
            })
            .collect(Collectors.toList());

        summary.setTotalItems(totalItems);
        summary.setTotalPrice(totalPrice);
        summary.setTotalChannels(channelSummaries.size());
        summary.setChannels(channelSummaries);

        return summary;
    }
}
