package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    // Not Optional<GoodsReceipt>/findByGateEntryId — a gate entry is not guaranteed to have at
    // most one goods receipt in the live data (confirmed: some gate entries have several, up to
    // 13 in one case), so a singular derived query throws NonUniqueResultException. Every caller
    // needs to handle 0..N.
    List<GoodsReceipt> findAllByGateEntryId(Long gateEntryId);
    boolean existsByGateEntryId(Long gateEntryId);
}
