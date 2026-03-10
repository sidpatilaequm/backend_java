package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Location;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    List<Location> findBySuperAdminAndIsActiveTrue(SuperAdmin superAdmin);
    
    List<Location> findBySuperAdmin(SuperAdmin superAdmin);
    
    Optional<Location> findByLocationIdAndSuperAdmin(Long locationId, SuperAdmin superAdmin);
    
    @Query("SELECT l FROM Location l WHERE l.superAdmin = :superAdmin AND l.isActive = true ORDER BY l.locationName ASC")
    List<Location> findActiveLocationsBySuperAdmin(@Param("superAdmin") SuperAdmin superAdmin);
    
    @Query("SELECT l FROM Location l WHERE l.superAdmin = :superAdmin AND " +
           "(LOWER(l.locationName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.state) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND l.isActive = true")
    List<Location> searchActiveLocationsBySuperAdmin(@Param("superAdmin") SuperAdmin superAdmin, 
                                                     @Param("searchTerm") String searchTerm);
    
    boolean existsByLocationNameAndSuperAdminAndIsActiveTrue(String locationName, SuperAdmin superAdmin);
    
    boolean existsByLocationNameAndSuperAdmin(String locationName, SuperAdmin superAdmin);
    
    Optional<Location> findByLocationNameIgnoreCaseAndSuperAdminAndIsActiveTrue(String locationName, SuperAdmin superAdmin);
    
    @Query("SELECT l FROM Location l WHERE LOWER(TRIM(l.locationName)) = LOWER(TRIM(:locationName)) AND l.superAdmin = :superAdmin AND l.isActive = true")
    Optional<Location> findByLocationNameTrimmedIgnoreCaseAndSuperAdminAndIsActiveTrue(@Param("locationName") String locationName, @Param("superAdmin") SuperAdmin superAdmin);
}
