package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "material_attribute")
public class MaterialAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;
    
    @Column(name = "attribute_value")
    private String attributeValue;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private MaterialVariant variant;
} 