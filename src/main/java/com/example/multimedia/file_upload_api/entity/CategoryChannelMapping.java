package com.example.multimedia.file_upload_api.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_channel_mapping", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"internal_category_id", "channel_id"}))
@Data
public class CategoryChannelMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_category_id", nullable = false)
    private ItemCategory internalCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_category_id", nullable = false)
    private ChannelCategory channelCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
