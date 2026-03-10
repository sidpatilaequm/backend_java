package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Find orders by company
    List<Order> findByCompanyId(Long companyId);
    
    // Find orders by company and status
    List<Order> findByCompanyIdAndOrderStatus(Long companyId, Order.OrderStatus orderStatus);
    
    // Find orders by company and date range
    List<Order> findByCompanyIdAndOrderDateBetween(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find orders by customer email
    List<Order> findByCustomerEmail(String customerEmail);
    
    // Find orders by channel
    List<Order> findByChannelId(Long channelId);
    
    // Find orders by company and channel
    List<Order> findByCompanyIdAndChannelId(Long companyId, Long channelId);
    
    // Check if order number exists
    boolean existsByOrderNumber(String orderNumber);
    
    // Get order count by company
    @Query("SELECT COUNT(o) FROM Order o WHERE o.companyId = :companyId")
    Long countByCompanyId(@Param("companyId") Long companyId);
    
    // Get total sales by company
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.companyId = :companyId")
    Double getTotalSalesByCompanyId(@Param("companyId") Long companyId);
    
    // Get orders with items (for detailed view)
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "WHERE o.orderId = :orderId")
    Optional<Order> findOrderWithItems(@Param("orderId") Long orderId);
    
    // Get orders with items by order number
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findOrderWithItemsByOrderNumber(@Param("orderNumber") String orderNumber);
    
    // Get orders by company with items
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "WHERE o.companyId = :companyId " +
           "ORDER BY o.orderDate DESC")
    List<Order> findOrdersByCompanyWithItems(@Param("companyId") Long companyId);
    
    // Get recent orders by company
    @Query("SELECT o FROM Order o " +
           "WHERE o.companyId = :companyId " +
           "ORDER BY o.orderDate DESC " +
           "LIMIT :limit")
    List<Order> findRecentOrdersByCompany(@Param("companyId") Long companyId, @Param("limit") int limit);
}
