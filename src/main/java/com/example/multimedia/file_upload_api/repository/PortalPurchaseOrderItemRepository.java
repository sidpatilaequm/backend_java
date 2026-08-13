package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PortalPurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalPurchaseOrderItemRepository extends JpaRepository<PortalPurchaseOrderItem, Long> {
    List<PortalPurchaseOrderItem> findByPurchaseOrder_Id(Long poId);
}
