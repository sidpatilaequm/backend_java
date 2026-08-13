package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ItrDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItrDetailsRepository extends JpaRepository<ItrDetails, Long> {
    Optional<ItrDetails> findByCompanyCompanyId(Long companyId);
    Optional<ItrDetails> findByPan(String pan);
}
