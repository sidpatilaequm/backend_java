package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.CertificateOfIncorporation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateOfIncorporationRepository extends JpaRepository<CertificateOfIncorporation, Long> {
    CertificateOfIncorporation findByCinNumber(String cinNumber);
    CertificateOfIncorporation findByCompanyCompanyId(Long companyId);
} 