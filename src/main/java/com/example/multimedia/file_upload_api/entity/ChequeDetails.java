package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChequeDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chequeDetailsId;

    @OneToOne
    @JoinColumn(name = "company_id")
    private CompanyDetails company;

    private String bank;
    private String code;
    private String issuedTo;
    private String signatory;
    private String accountNumber;
    private String ifsc;
    private String issued;
    private String branch;
}
