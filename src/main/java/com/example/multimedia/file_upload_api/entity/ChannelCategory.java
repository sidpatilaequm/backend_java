package com.example.multimedia.file_upload_api.entity;

import com.example.multimedia.file_upload_api.enums.CategoryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "channel_category", indexes = {
        @Index(name = "idx_channel_parent", columnList = "channel_id, parent_category_id"),
        @Index(name = "idx_channel_path", columnList = "channel_id, full_path")
})
public class ChannelCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_code", nullable = false)
    private String categoryCode;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CompanyDetails company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private ChannelCategory parentCategory;

    @Column(name = "level_no")
    private Integer levelNo;

    @Column(name = "full_path")
    private String fullPath;

    @Column(name = "external_category_id")
    private String externalCategoryId;

    @Column(name = "external_parent_id")
    private String externalParentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private CategoryType categoryType = CategoryType.LEAF;

    @Column(name = "is_leaf")
    private Boolean isLeaf = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
