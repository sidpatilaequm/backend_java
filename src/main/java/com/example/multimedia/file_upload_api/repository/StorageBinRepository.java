package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.StorageBin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface StorageBinRepository extends JpaRepository<StorageBin, StorageBin.Pk> {
    Page<StorageBin> findByWarehouseNoAndBinCodeContainingIgnoreCase(String warehouseNo, String search, Pageable pageable);
    Page<StorageBin> findByWarehouseNo(String warehouseNo, Pageable pageable);
    long countByWarehouseNo(String warehouseNo);
    long deleteByWarehouseNo(String warehouseNo);
    List<StorageBin> findByWarehouseNoAndBinCodeIn(String warehouseNo, Collection<String> binCodes);
}
