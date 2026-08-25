package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Optional<GoodsReceipt> findByGateEntryId(Long gateEntryId);
    boolean existsByGateEntryId(Long gateEntryId);
}
