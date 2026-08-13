package com.example.multimedia.file_upload_api.entity.sap;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_sync_logs")
@Getter
@Setter
public class PaymentSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "sync_type", length = 30)
    private String syncType;

    @Column(name = "records_fetched")
    private Integer recordsFetched;

    @Column(name = "sync_status", length = 20)
    private String syncStatus;

    @Column(name = "sap_api_name", length = 100)
    private String sapApiName;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
