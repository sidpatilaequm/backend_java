package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, String> {
    List<Plant> findByCompany_CompanyCode(String companyCode);
}
