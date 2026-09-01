package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "gate_entries")
public class GateEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_id", nullable = false)
    private Asn asn;

    @Column(name = "gate_pass_number", unique = true)
    private String gatePassNumber;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "decision", nullable = false)
    private String decision; // ALLOW, HOLD, REJECT

    // Store documents mapping as JSON string
    @Column(name = "documents", columnDefinition = "TEXT")
    private String documents;

    @Column(name = "declared_packages")
    private Integer declaredPackages;

    @Column(name = "counted_packages")
    private Integer countedPackages;

    @Column(name = "package_remark", columnDefinition = "TEXT")
    private String packageRemark;

    @Column(name = "hold_reason", columnDefinition = "TEXT")
    private String holdReason;

    @Column(name = "in_time")
    private LocalDateTime inTime;

    @Column(name = "processed_by")
    private String processedBy;

    @Column(name = "supervisor_remark", columnDefinition = "TEXT")
    private String supervisorRemark;

    @OneToMany(mappedBy = "gateEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GateEntryLine> lines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
