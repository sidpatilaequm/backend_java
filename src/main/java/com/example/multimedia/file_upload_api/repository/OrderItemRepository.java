package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Find order items by order
    List<OrderItem> findByOrder_OrderId(Long orderId);
    
    // Find order items by order number
    List<OrderItem> findByOrder_OrderNumber(String orderNumber);
    
    // Find order items by material
    List<OrderItem> findByMaterialId(Long materialId);
    
    // Find order items by channel
    List<OrderItem> findByChannelId(Long channelId);
    
    // Find order items by company (through order)
    @Query("SELECT oi FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.companyId = :companyId")
    List<OrderItem> findByCompanyId(@Param("companyId") Long companyId);
    
    // Get total quantity sold for a material
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.materialId = :materialId")
    Long getTotalQuantitySoldByMaterialId(@Param("materialId") Long materialId);
    
    // Get total sales for a material
    @Query("SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi WHERE oi.materialId = :materialId")
    Double getTotalSalesByMaterialId(@Param("materialId") Long materialId);
}
