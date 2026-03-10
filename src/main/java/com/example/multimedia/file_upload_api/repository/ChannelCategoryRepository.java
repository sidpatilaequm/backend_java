package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ChannelCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelCategoryRepository extends JpaRepository<ChannelCategory, Long> {
    List<ChannelCategory> findByChannel_ChannelId(Long channelId);
    List<ChannelCategory> findByChannel_ChannelIdAndIsActive(Long channelId, Boolean isActive);
    Optional<ChannelCategory> findByCategoryCodeAndChannel_ChannelId(String categoryCode, Long channelId);
    boolean existsByCategoryCodeAndChannel_ChannelId(String categoryCode, Long channelId);
    boolean existsByCategoryCodeIgnoreCaseAndChannel_ChannelId(String categoryCode, Long channelId);
}
