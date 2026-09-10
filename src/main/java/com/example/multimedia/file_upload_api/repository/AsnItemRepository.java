package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.AsnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

@Repository
public interface AsnItemRepository extends JpaRepository<AsnItem, Long> {

    @Query("SELECT COALESCE(SUM(a.quantityShipped), 0) FROM AsnItem a WHERE a.purchaseOrderItem.id = :poItemId")
    BigDecimal getTotalShippedQuantityForPoItem(@Param("poItemId") Long poItemId);

    @Query("SELECT COALESCE(SUM(a.quantityShipped), 0) FROM AsnItem a WHERE a.purchaseOrderItem.id = :poItemId AND a.asn.status = 'RECEIVED'")
    BigDecimal getReceivedQuantity(@Param("poItemId") Long poItemId);

    @Query("SELECT COALESCE(SUM(a.quantityShipped), 0) FROM AsnItem a WHERE a.purchaseOrderItem.id = :poItemId AND a.asn.status IN ('IN_TRANSIT', 'BUYER_APPROVAL_PENDING')")
    BigDecimal getInTransitQuantity(@Param("poItemId") Long poItemId);
}
