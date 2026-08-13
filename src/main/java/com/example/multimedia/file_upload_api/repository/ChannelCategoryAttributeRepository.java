package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ChannelCategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelCategoryAttributeRepository extends JpaRepository<ChannelCategoryAttribute, Long> {
    List<ChannelCategoryAttribute> findByChannelCategory_CategoryId(Long categoryId);
}
