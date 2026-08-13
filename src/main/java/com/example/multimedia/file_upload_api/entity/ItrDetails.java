package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_itr")
@Getter
@Setter
public class ItrDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itrDetailsId;

    @OneToOne
    @JoinColumn(name = "company_id")
    private CompanyDetails company;

    private String pan;
    private String birthOrIncorporatedDate;
    private String name;
    
    private String fy;
    private Boolean itrFiled;
    private String itrType;
    private String grossTurnover;
    private String grossTurnoverFormatted;
    private String exportTurnover;
    private String exportTurnoverFormatted;
    private Boolean valid;
    private String panStatus;
    private String message;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
