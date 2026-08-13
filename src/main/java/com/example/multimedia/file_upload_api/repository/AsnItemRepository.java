package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.AsnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

@Repository
public interface AsnItemRepository extends JpaRepository<AsnItem, Long> {

    @Query("SELECT COALESCE(SUM(a.quantityShipped), 0) FROM AsnItem a WHERE a.purchaseOrderItem.purchaseOrder.poNumber = :poNumber AND a.purchaseOrderItem.lineNumber = :lineNumber")
    BigDecimal getTotalShippedQuantityForPoLine(@Param("poNumber") String poNumber, @Param("lineNumber") Integer lineNumber);

    @Query("SELECT COALESCE(SUM(a.quantityShipped), 0) FROM AsnItem a WHERE a.purchaseOrderItem.purchaseOrder.poNumber = :poNumber AND a.purchaseOrderItem.lineNumber = :lineNumber AND a.asn.status = 'RECEIVED'")
    BigDecimal getReceivedQuantity(@Param("poNumber") String poNumber, @Param("lineNumber") Integer lineNumber);

    @Query("SELECT COALESCE(SUM(a.quantityShipped), 0) FROM AsnItem a WHERE a.purchaseOrderItem.purchaseOrder.poNumber = :poNumber AND a.purchaseOrderItem.lineNumber = :lineNumber AND a.asn.status IN ('IN_TRANSIT', 'BUYER_APPROVAL_PENDING')")
    BigDecimal getInTransitQuantity(@Param("poNumber") String poNumber, @Param("lineNumber") Integer lineNumber);
}
