package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.MasterBomFile;
import com.example.multimedia.file_upload_api.entity.MasterBomRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterBomRecordRepository extends JpaRepository<MasterBomRecord, Long> {

    List<MasterBomRecord> findByMasterFileAndFgNumber(MasterBomFile masterFile, String fgNumber);

    @Query("SELECT r FROM MasterBomRecord r WHERE r.masterFile = :masterFile AND REPLACE(r.fgNumber, ' ', '') = REPLACE(:fgNumber, ' ', '')")
    List<MasterBomRecord> findByMasterFileAndFgNumberNormalized(@Param("masterFile") MasterBomFile masterFile,
            @Param("fgNumber") String fgNumber);

    @Query("SELECT r FROM MasterBomRecord r WHERE r.masterFile = :masterFile AND r.fgNumber LIKE :prefix%")
    List<MasterBomRecord> findByMasterFileAndFgNumberStartingWith(@Param("masterFile") MasterBomFile masterFile,
            @Param("prefix") String prefix);
}
