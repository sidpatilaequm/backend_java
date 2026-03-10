package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PanDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long panDetailsId;

    @OneToOne
    @JoinColumn(name = "company_id")
    private CompanyDetails company;

    private String panNumber;
    private String name;
    private String dateOfBirthIncorporation;
    private String fathersName;
    private String category;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
