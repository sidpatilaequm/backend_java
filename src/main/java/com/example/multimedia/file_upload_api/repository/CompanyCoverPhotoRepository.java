package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.CompanyCoverPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyCoverPhotoRepository extends JpaRepository<CompanyCoverPhoto, Long> {

    /**
     * Find all active cover photos for a company
     */
    @Query("SELECT c FROM CompanyCoverPhoto c WHERE c.companyId = :companyId AND c.isActive = true ORDER BY c.sequenceOrder ASC")
    List<CompanyCoverPhoto> findByCompanyIdAndIsActiveTrueOrderBySequenceOrderAsc(@Param("companyId") Long companyId);

    /**
     * Find the primary cover photo for a company (first in sequence)
     */
    @Query("SELECT c FROM CompanyCoverPhoto c WHERE c.companyId = :companyId AND c.isActive = true ORDER BY c.sequenceOrder ASC LIMIT 1")
    Optional<CompanyCoverPhoto> findPrimaryCoverPhotoByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find cover photo by company ID and cover photo name
     */
    Optional<CompanyCoverPhoto> findByCompanyIdAndCoverPhotoNameAndIsActiveTrue(Long companyId, String coverPhotoName);

    /**
     * Count active cover photos for a company
     */
    @Query("SELECT COUNT(c) FROM CompanyCoverPhoto c WHERE c.companyId = :companyId AND c.isActive = true")
    Long countActiveCoverPhotosByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find maximum sequence order for a company
     */
    @Query("SELECT MAX(c.sequenceOrder) FROM CompanyCoverPhoto c WHERE c.companyId = :companyId AND c.isActive = true")
    Long findMaxSequenceOrderByCompanyId(@Param("companyId") Long companyId);
}
