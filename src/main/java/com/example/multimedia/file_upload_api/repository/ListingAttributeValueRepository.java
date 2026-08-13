package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.ListingAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingAttributeValueRepository extends JpaRepository<ListingAttributeValue, Long> {
    List<ListingAttributeValue> findByListing_Id(Long listingId);
}
