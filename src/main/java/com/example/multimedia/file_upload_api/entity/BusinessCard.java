package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "business_cards")
public class BusinessCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(name = "company_name")
    private String companyName;
    
    private String address;
    
    @ElementCollection
    @CollectionTable(name = "business_card_phone_numbers", 
                    joinColumns = @JoinColumn(name = "business_card_id"))
    @Column(name = "phone_number")
    private List<String> phoneNumbers;
    
    @ElementCollection
    @CollectionTable(name = "business_card_email_addresses", 
                    joinColumns = @JoinColumn(name = "business_card_id"))
    @Column(name = "email_address")
    private List<String> emailAddresses;
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private CompanyDetails company;
} 