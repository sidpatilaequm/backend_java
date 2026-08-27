package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
}
