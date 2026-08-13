package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.MaterialListingDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;

public interface MaterialListingService {
    ServiceResponse createListing(MaterialListingDTO dto);
    ServiceResponse updateListingStatus(Long listingId, String status, Long companyId);
    ServiceResponse getListingsByMaterial(Long materialId, Long companyId);
    ServiceResponse getListingsByChannel(Long channelId, Long companyId);
}
