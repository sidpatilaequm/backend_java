package com.example.multimedia.file_upload_api.entity;

import com.example.multimedia.file_upload_api.enums.ListingStatus;
import com.example.multimedia.file_upload_api.enums.SyncStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_channel_listing", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"material_id", "channel_id"}),
           @UniqueConstraint(columnNames = {"channel_id", "channel_sku"})
       },
       indexes = {
           @Index(name = "idx_listing_material", columnList = "material_id"),
           @Index(name = "idx_listing_channel", columnList = "channel_id"),
           @Index(name = "idx_listing_category", columnList = "channel_category_id"),
           @Index(name = "idx_listing_status", columnList = "listing_status, sync_status")
       })
@Data
public class MaterialChannelListing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyDetails company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_category_id", nullable = true)
    private ChannelCategory channelCategory;
    
    @Column(name = "channel_sku")
    private String channelSku;
    
    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;
    
    @Column(name = "mrp", precision = 12, scale = 2)
    private BigDecimal mrp;
    
    @Column(name = "available_stock")
    private Integer availableStock;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "listing_status")
    private ListingStatus listingStatus = ListingStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private SyncStatus syncStatus = SyncStatus.PENDING;
    
    @Column(name = "validation_status")
    private String validationStatus;
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
