package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.MaterialListingDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.enums.ListingStatus;
import com.example.multimedia.file_upload_api.enums.SyncStatus;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.service.MaterialListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialListingServiceImpl implements MaterialListingService {

    private final MaterialChannelListingRepository listingRepository;
    private final MaterialRepository materialRepository;
    private final ChannelRepository channelRepository;
    private final ChannelCategoryRepository categoryRepository;
    private final CompanyDetailsRepository companyRepository;

    @Override
    @Transactional
    public ServiceResponse createListing(MaterialListingDTO dto) {
        // Validation: Only leaf categories allowed
        ChannelCategory category = categoryRepository.findById(dto.getChannelCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (!category.getIsLeaf()) {
            return ServiceResponse.builder()
                    .status(false)
                    .message("Listing can only be created for leaf categories")
                    .build();
        }

        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new RuntimeException("Material not found"));
        Channel channel = channelRepository.findById(dto.getChannelId())
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        CompanyDetails company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!channel.getCompany().getCompanyId().equals(dto.getCompanyId()) ||
            !category.getChannel().getCompany().getCompanyId().equals(dto.getCompanyId())) {
            return ServiceResponse.builder().status(false).message("Unauthorized company access").build();
        }

        MaterialChannelListing listing = new MaterialChannelListing();
        listing.setMaterial(material);
        listing.setChannel(channel);
        listing.setChannelCategory(category);
        listing.setCompany(company);
        listing.setChannelSku(dto.getChannelSku());
        listing.setSellingPrice(dto.getSellingPrice());
        listing.setMrp(dto.getMrp());
        listing.setAvailableStock(dto.getAvailableStock());
        listing.setListingStatus(ListingStatus.DRAFT);
        listing.setSyncStatus(SyncStatus.PENDING);

        MaterialChannelListing saved = listingRepository.save(listing);
        return ServiceResponse.builder()
                .status(true)
                .message("Listing created successfully")
                .data(mapToDTO(saved))
                .build();
    }

    @Override
    @Transactional
    public ServiceResponse updateListingStatus(Long listingId, String status, Long companyId) {
        MaterialChannelListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        
        if (!listing.getCompany().getCompanyId().equals(companyId)) {
            return ServiceResponse.builder().status(false).message("Unauthorized").build();
        }

        listing.setListingStatus(ListingStatus.valueOf(status));
        listingRepository.save(listing);
        
        return ServiceResponse.builder()
                .status(true)
                .message("Listing status updated to " + status)
                .build();
    }

    @Override
    public ServiceResponse getListingsByMaterial(Long materialId, Long companyId) {
        List<MaterialChannelListing> listings = listingRepository.findByMaterial_MaterialIdAndCompany_CompanyId(materialId, companyId);
        return ServiceResponse.builder()
                .status(true)
                .data(listings.stream().map(this::mapToDTO).collect(Collectors.toList()))
                .build();
    }

    @Override
    public ServiceResponse getListingsByChannel(Long channelId, Long companyId) {
        List<MaterialChannelListing> listings = listingRepository.findByChannel_ChannelIdAndCompany_CompanyId(channelId, companyId);
        return ServiceResponse.builder()
                .status(true)
                .data(listings.stream().map(this::mapToDTO).collect(Collectors.toList()))
                .build();
    }

    private MaterialListingDTO mapToDTO(MaterialChannelListing listing) {
        MaterialListingDTO dto = new MaterialListingDTO();
        dto.setId(listing.getId());
        dto.setMaterialId(listing.getMaterial().getMaterialId());
        dto.setChannelId(listing.getChannel().getChannelId());
        dto.setChannelCategoryId(listing.getChannelCategory() != null ? listing.getChannelCategory().getCategoryId() : null);
        dto.setCompanyId(listing.getCompany().getCompanyId());
        dto.setChannelSku(listing.getChannelSku());
        dto.setSellingPrice(listing.getSellingPrice());
        dto.setMrp(listing.getMrp());
        dto.setAvailableStock(listing.getAvailableStock());
        dto.setListingStatus(listing.getListingStatus());
        dto.setSyncStatus(listing.getSyncStatus());
        dto.setValidationStatus(listing.getValidationStatus());
        return dto;
    }
}
