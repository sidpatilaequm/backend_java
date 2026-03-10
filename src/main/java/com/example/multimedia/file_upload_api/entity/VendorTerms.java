package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
public class VendorTerms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorTermsId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserDetail user;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyDetails company;

    private Boolean isActive = true;

    @Lob
    @Column(name = "payment_terms_file", columnDefinition = "LONGBLOB")
    private byte[] paymentTermsFile;

    @Lob
    @Column(name = "incoterms_file", columnDefinition = "LONGBLOB")
    private byte[] incotermsFile;

    @Lob
    @Column(name = "delivery_terms_file", columnDefinition = "LONGBLOB")
    private byte[] deliveryTermsFile;

    private String paymentTermsFileName;
    private String paymentTermsFileType;

    private String incotermsFileName;
    private String incotermsFileType;

    private String deliveryTermsFileName;
    private String deliveryTermsFileType;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime modifiedDate;

    // Getters and Setters
    public Long getVendorTermsId() {
        return vendorTermsId;
    }

    public void setVendorTermsId(Long vendorTermsId) {
        this.vendorTermsId = vendorTermsId;
    }

    public UserDetail getUser() {
        return user;
    }

    public void setUser(UserDetail user) {
        this.user = user;
    }

    public CompanyDetails getCompany() {
        return company;
    }

    public void setCompany(CompanyDetails company) {
        this.company = company;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public byte[] getPaymentTermsFile() {
        return paymentTermsFile;
    }

    public void setPaymentTermsFile(byte[] paymentTermsFile) {
        this.paymentTermsFile = paymentTermsFile;
    }

    public byte[] getIncotermsFile() {
        return incotermsFile;
    }

    public void setIncotermsFile(byte[] incotermsFile) {
        this.incotermsFile = incotermsFile;
    }

    public byte[] getDeliveryTermsFile() {
        return deliveryTermsFile;
    }

    public void setDeliveryTermsFile(byte[] deliveryTermsFile) {
        this.deliveryTermsFile = deliveryTermsFile;
    }

    public String getPaymentTermsFileName() {
        return paymentTermsFileName;
    }

    public void setPaymentTermsFileName(String paymentTermsFileName) {
        this.paymentTermsFileName = paymentTermsFileName;
    }

    public String getPaymentTermsFileType() {
        return paymentTermsFileType;
    }

    public void setPaymentTermsFileType(String paymentTermsFileType) {
        this.paymentTermsFileType = paymentTermsFileType;
    }

    public String getIncotermsFileName() {
        return incotermsFileName;
    }

    public void setIncotermsFileName(String incotermsFileName) {
        this.incotermsFileName = incotermsFileName;
    }

    public String getIncotermsFileType() {
        return incotermsFileType;
    }

    public void setIncotermsFileType(String incotermsFileType) {
        this.incotermsFileType = incotermsFileType;
    }

    public String getDeliveryTermsFileName() {
        return deliveryTermsFileName;
    }

    public void setDeliveryTermsFileName(String deliveryTermsFileName) {
        this.deliveryTermsFileName = deliveryTermsFileName;
    }

    public String getDeliveryTermsFileType() {
        return deliveryTermsFileType;
    }

    public void setDeliveryTermsFileType(String deliveryTermsFileType) {
        this.deliveryTermsFileType = deliveryTermsFileType;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
