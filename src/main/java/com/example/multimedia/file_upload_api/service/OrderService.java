package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.CheckoutRequest;
import com.example.multimedia.file_upload_api.dto.CheckoutResponse;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.Order;
import com.example.multimedia.file_upload_api.entity.OrderItem;
import com.example.multimedia.file_upload_api.repository.OrderRepository;
import com.example.multimedia.file_upload_api.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * Process checkout and create order
     */
    public ServiceResponse processCheckout(CheckoutRequest request) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Validate request
            if (request == null || request.getCustomerInfo() == null || 
                request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Invalid checkout request");
                response.setErrorCode("INVALID_REQUEST");
                return response;
            }

            // Generate unique 6-digit order number
            String orderNumber = generateUniqueOrderNumber();
            
            // Create order entity
            Order order = createOrderFromRequest(request, orderNumber);
            
            // Save order
            Order savedOrder = orderRepository.save(order);
            
            // Create order items
            List<OrderItem> orderItems = createOrderItemsFromRequest(request, savedOrder);
            orderItemRepository.saveAll(orderItems);
            
            // Set order items to order for response
            savedOrder.setOrderItems(orderItems);
            
            // Create response
            CheckoutResponse checkoutResponse = createCheckoutResponse(savedOrder);
            
            response.setStatus("SUCCESS");
            response.setStatusMsg("Order created successfully");
            response.addData("orderNumber", checkoutResponse.getOrderNumber());
            response.addData("orderId", checkoutResponse.getOrderId());
            response.addData("orderDetails", checkoutResponse.getOrderDetails());

        } catch (Exception e) {
            logger.error("Error processing checkout: ", e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to process checkout");
            response.setErrorCode("CHECKOUT_ERROR");
        }

        return response;
    }

    /**
     * Generate unique 6-digit order number
     */
    private String generateUniqueOrderNumber() {
        String orderNumber;
        do {
            orderNumber = generateSixDigitNumber();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        
        return orderNumber;
    }

    /**
     * Generate random 6-digit number
     */
    private String generateSixDigitNumber() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // Generates number between 100000 and 999999
        return String.valueOf(number);
    }

    /**
     * Create Order entity from request
     */
    private Order createOrderFromRequest(CheckoutRequest request, String orderNumber) {
        Order order = new Order();
        
        // Set order number
        order.setOrderNumber(orderNumber);
        
        // Set customer information
        CheckoutRequest.CustomerInfo customerInfo = request.getCustomerInfo();
        order.setCustomerFirstName(customerInfo.getFirstName());
        order.setCustomerLastName(customerInfo.getLastName());
        order.setCustomerEmail(customerInfo.getEmail());
        order.setCustomerPhone(customerInfo.getPhone());
        order.setCustomerAddress(customerInfo.getAddress());
        order.setCustomerCity(customerInfo.getCity());
        order.setCustomerState(customerInfo.getState());
        order.setCustomerZipCode(customerInfo.getZipCode());
        order.setCustomerCountry(customerInfo.getCountry());
        order.setCustomerNotes(customerInfo.getNotes());
        
        // Set order summary
        CheckoutRequest.OrderSummary orderSummary = request.getOrderSummary();
        order.setTotalItems(orderSummary.getTotalItems());
        order.setSubtotal(orderSummary.getSubtotal());
        order.setShipping(orderSummary.getShipping());
        order.setTax(orderSummary.getTax());
        order.setDiscount(orderSummary.getDiscount());
        order.setTotal(orderSummary.getTotal());
        
        // Set order metadata
        CheckoutRequest.OrderMetadata orderMetadata = request.getOrderMetadata();
        order.setOrderDate(orderMetadata.getOrderDate());
        order.setChannelId(orderMetadata.getChannelId());
        order.setCompanyId(orderMetadata.getCompanyId());
        
        // Set default status
        order.setOrderStatus(Order.OrderStatus.PENDING);
        
        return order;
    }

    /**
     * Create OrderItem entities from request
     */
    private List<OrderItem> createOrderItemsFromRequest(CheckoutRequest request, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CheckoutRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMaterialId(itemRequest.getMaterialId());
            orderItem.setMaterialName(itemRequest.getMaterialName());
            orderItem.setMaterialCode(itemRequest.getMaterialCode());
            orderItem.setPrice(itemRequest.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setChannelId(itemRequest.getChannelId());
            orderItem.setImageId(null); // Will be set if image data is provided
            orderItem.setImageName(itemRequest.getImageName());
            orderItem.setImageType(itemRequest.getImageType());
            orderItem.setImageBase64(itemRequest.getImageBase64());
            orderItem.setAddedAt(itemRequest.getAddedAt());
            
            // Calculate total price
            orderItem.calculateTotalPrice();
            
            orderItems.add(orderItem);
        }
        
        return orderItems;
    }

    /**
     * Create checkout response from saved order
     */
    private CheckoutResponse createCheckoutResponse(Order order) {
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderNumber(order.getOrderNumber());
        response.setOrderId(order.getOrderId().toString());
        response.setStatus("SUCCESS");
        response.setMessage("Order created successfully");
        
        // Create order details
        CheckoutResponse.OrderDetails orderDetails = new CheckoutResponse.OrderDetails();
        
        // Set customer info
        CheckoutResponse.CustomerDetails customerDetails = new CheckoutResponse.CustomerDetails();
        customerDetails.setFirstName(order.getCustomerFirstName());
        customerDetails.setLastName(order.getCustomerLastName());
        customerDetails.setEmail(order.getCustomerEmail());
        customerDetails.setPhone(order.getCustomerPhone());
        customerDetails.setAddress(order.getCustomerAddress());
        customerDetails.setCity(order.getCustomerCity());
        customerDetails.setState(order.getCustomerState());
        customerDetails.setZipCode(order.getCustomerZipCode());
        customerDetails.setCountry(order.getCustomerCountry());
        customerDetails.setNotes(order.getCustomerNotes());
        orderDetails.setCustomerInfo(customerDetails);
        
        // Set order items
        List<CheckoutResponse.OrderItemDetails> orderItemDetails = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            CheckoutResponse.OrderItemDetails itemDetails = new CheckoutResponse.OrderItemDetails();
            itemDetails.setMaterialId(orderItem.getMaterialId());
            itemDetails.setMaterialName(orderItem.getMaterialName());
            itemDetails.setMaterialCode(orderItem.getMaterialCode());
            itemDetails.setPrice(orderItem.getPrice());
            itemDetails.setQuantity(orderItem.getQuantity());
            itemDetails.setTotalPrice(orderItem.getTotalPrice());
            itemDetails.setChannelId(orderItem.getChannelId());
            itemDetails.setImageName(orderItem.getImageName());
            itemDetails.setImageType(orderItem.getImageType());
            itemDetails.setAddedAt(orderItem.getAddedAt());
            orderItemDetails.add(itemDetails);
        }
        orderDetails.setOrderItems(orderItemDetails);
        
        // Set order summary
        CheckoutResponse.OrderSummaryDetails orderSummary = new CheckoutResponse.OrderSummaryDetails();
        orderSummary.setTotalItems(order.getTotalItems());
        orderSummary.setSubtotal(order.getSubtotal());
        orderSummary.setShipping(order.getShipping());
        orderSummary.setTax(order.getTax());
        orderSummary.setDiscount(order.getDiscount());
        orderSummary.setTotal(order.getTotal());
        orderDetails.setOrderSummary(orderSummary);
        
        // Set order metadata
        CheckoutResponse.OrderMetadataDetails orderMetadata = new CheckoutResponse.OrderMetadataDetails();
        orderMetadata.setOrderDate(order.getOrderDate());
        orderMetadata.setChannelId(order.getChannelId());
        orderMetadata.setCompanyId(order.getCompanyId());
        orderMetadata.setOrderStatus(order.getOrderStatus().toString());
        orderDetails.setOrderMetadata(orderMetadata);
        
        response.setOrderDetails(orderDetails);
        
        return response;
    }

    /**
     * Get order by order number
     */
    public ServiceResponse getOrderByOrderNumber(String orderNumber) {
        ServiceResponse response = new ServiceResponse();

        try {
            Optional<Order> orderOpt = orderRepository.findOrderWithItemsByOrderNumber(orderNumber);
            if (orderOpt.isEmpty()) {
                response.setStatus("ERROR");
                response.setStatusMsg("Order not found");
                response.setErrorCode("ORDER_NOT_FOUND");
                return response;
            }

            Order order = orderOpt.get();
            CheckoutResponse checkoutResponse = createCheckoutResponse(order);
            
            response.setStatus("SUCCESS");
            response.setStatusMsg("Order retrieved successfully");
            response.addData("orderDetails", checkoutResponse.getOrderDetails());

        } catch (Exception e) {
            logger.error("Error retrieving order: ", e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve order");
            response.setErrorCode("GET_ORDER_ERROR");
        }

        return response;
    }

    /**
     * Get orders by company
     */
    public ServiceResponse getOrdersByCompany(Long companyId) {
        ServiceResponse response = new ServiceResponse();

        try {
            List<Order> orders = orderRepository.findOrdersByCompanyWithItems(companyId);
            
            List<CheckoutResponse> orderResponses = new ArrayList<>();
            for (Order order : orders) {
                CheckoutResponse checkoutResponse = createCheckoutResponse(order);
                orderResponses.add(checkoutResponse);
            }
            
            response.setStatus("SUCCESS");
            response.setStatusMsg("Orders retrieved successfully");
            response.addData("orders", orderResponses);
            response.addData("totalOrders", orders.size());

        } catch (Exception e) {
            logger.error("Error retrieving orders by company: ", e);
            response.setStatus("ERROR");
            response.setStatusMsg("Failed to retrieve orders");
            response.setErrorCode("GET_ORDERS_ERROR");
        }

        return response;
    }
}
