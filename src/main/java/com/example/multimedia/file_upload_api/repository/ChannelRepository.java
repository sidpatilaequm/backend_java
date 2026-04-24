package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Optional<Channel> findByChannelCode(String channelCode);

    boolean existsByChannelCode(String channelCode);

    boolean existsByChannelCodeIgnoreCase(String channelCode);

    // Company and user isolation methods
    List<Channel> findByCompany_CompanyId(Long companyId);

    List<Channel> findByUser_UserId(Long userId);

    List<Channel> findByCompany_CompanyIdAndUser_UserId(Long companyId, Long userId);

    List<Channel> findByCompany_CompanyIdAndIsActive(Long companyId, Boolean isActive);

    List<Channel> findByUser_UserIdAndIsActive(Long userId, Boolean isActive);

    List<Channel> findByCompany_CompanyIdAndUser_UserIdAndIsActive(Long companyId, Long userId, Boolean isActive);

    Optional<Channel> findByChannelIdAndCompany_CompanyId(Long channelId, Long companyId);

    Optional<Channel> findByChannelIdAndUser_UserId(Long channelId, Long userId);

    Optional<Channel> findByChannelIdAndCompany_CompanyIdAndUser_UserId(Long channelId, Long companyId, Long userId);

    // Check if channel code exists for a specific company
    boolean existsByChannelCodeAndCompany_CompanyId(String channelCode, Long companyId);

    boolean existsByChannelCodeIgnoreCaseAndCompany_CompanyId(String channelCode, Long companyId);

    // Filter by SuperAdmin
    List<Channel> findByCompany_SuperAdmin_SuperAdminId(Long superAdminId);
}
