package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageLocationRepository extends JpaRepository<StorageLocation, StorageLocation.Pk> {
    List<StorageLocation> findByPlantCode(String plantCode);
}
