package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyDetailsRepository extends JpaRepository<CompanyDetails, Long> {

    List<CompanyDetails> findByGstinNumberAndUser_UserIdIn(String gstinNumber, List<Long> userIds);
    
    List<CompanyDetails> findByGstinNumber(String gstinNumber);

    Optional<CompanyDetails> findByCompanyIdAndUserUserId(Long companyId, Long userId);
    
    List<CompanyDetails> findByUserUserId(Long userId);

    @Query("SELECT cd FROM CompanyDetails cd JOIN cd.user u WHERE u.userId IN :userIds")
    List<CompanyDetails> findByUserUserIdIn(@Param("userIds") List<Long> userIds);

    List<CompanyDetails> findBySuperAdminSuperAdminId(Long superAdminId);

    List<CompanyDetails> findByAuthKey(String authKey);
    
    List<CompanyDetails> findBySuperAdmin_SuperAdminIdAndAuthKey(Long superAdminId, String authKey);
    
    // New method for data isolation - filter by GST number and super admin
    List<CompanyDetails> findByGstinNumberAndSuperAdmin_SuperAdminId(String gstinNumber, Long superAdminId);
    
    // New method for data isolation - filter by GST number, super admin, and auth key
    List<CompanyDetails> findByGstinNumberAndSuperAdmin_SuperAdminIdAndAuthKey(String gstinNumber, Long superAdminId, String authKey);
}