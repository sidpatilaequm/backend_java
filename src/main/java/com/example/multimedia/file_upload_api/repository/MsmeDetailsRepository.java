package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.MsmeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MsmeDetailsRepository extends JpaRepository<MsmeDetails, Long> {
    Optional<MsmeDetails> findByCompanyCompanyId(Long companyId);
    Optional<MsmeDetails> findByUdyamNumber(String udyamNumber);
}
