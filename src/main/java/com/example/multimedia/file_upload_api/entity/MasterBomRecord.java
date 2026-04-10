package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "master_bom_records", indexes = {
    @Index(name = "idx_fg_number", columnList = "fg_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterBomRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_file_id", nullable = false)
    private MasterBomFile masterFile;

    @Column(name = "fg_number", length = 100, nullable = false)
    private String fgNumber;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "rm_item_code", length = 100, nullable = false)
    private String rmItemCode;

    @Column(name = "rm_description", length = 255)
    private String rmDescription;

    @Column(name = "qty", columnDefinition = "DECIMAL(10,4)")
    private Double qty;

    @Column(name = "uom", length = 20)
    private String uom;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;
}
