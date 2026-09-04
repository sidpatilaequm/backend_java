package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PlantLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantLocationRepository extends JpaRepository<PlantLocation, PlantLocation.Pk> {
    List<PlantLocation> findByPlantCode(String plantCode);
}
