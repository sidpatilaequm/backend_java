package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {
    List<Warehouse> findByPlantCode(String plantCode);
    Optional<Warehouse> findByPlantCodeAndSlocId(String plantCode, String slocId);
}
