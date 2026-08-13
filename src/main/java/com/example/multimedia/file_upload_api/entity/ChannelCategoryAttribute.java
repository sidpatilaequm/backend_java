package com.example.multimedia.file_upload_api.entity;

import com.example.multimedia.file_upload_api.enums.MarketplaceAttributeType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "channel_category_attribute")
@Data
public class ChannelCategoryAttribute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_category_id", nullable = false)
    private ChannelCategory channelCategory;
    
    @Column(name = "attribute_name", nullable = false)
    private String attributeName;
    
    @Column(name = "attribute_code", nullable = false)
    private String attributeCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type")
    private MarketplaceAttributeType attributeType = MarketplaceAttributeType.TEXT;
    
    @Column(name = "is_required")
    private Boolean isRequired = false;
    
    @Column(name = "allowed_values", columnDefinition = "TEXT")
    private String allowedValues; // Store as JSON string
    
    @Column(name = "default_value")
    private String defaultValue;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
