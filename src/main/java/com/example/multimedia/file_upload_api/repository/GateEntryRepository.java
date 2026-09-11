package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.GateEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GateEntryRepository extends JpaRepository<GateEntry, Long> {
    List<GateEntry> findByInTimeBetween(LocalDateTime start, LocalDateTime end);
    List<GateEntry> findByDecision(String decision);
    Optional<GateEntry> findByGatePassNumber(String gatePassNumber);
    Optional<GateEntry> findFirstByAsnIdOrderByCreatedDateDesc(Long asnId);
    List<GateEntry> findByAsnId(Long asnId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT ge FROM GateEntry ge " +
            "LEFT JOIN FETCH ge.asn a " +
            "LEFT JOIN FETCH a.purchaseOrder po " +
            "LEFT JOIN FETCH po.vendor v " +
            "LEFT JOIN FETCH a.items " +
            "LEFT JOIN FETCH ge.lines " +
            "WHERE ge.id = :id")
    Optional<GateEntry> findWithDetailsById(@org.springframework.data.repository.query.Param("id") Long id);
}
