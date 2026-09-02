package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.FullProductCreateRequest;
import com.example.multimedia.file_upload_api.dto.ProductResponseDTO;
import com.example.multimedia.file_upload_api.entity.Material;
import java.util.List;

public interface ECommerceProductService {
    ProductResponseDTO createFullProduct(FullProductCreateRequest request);

    ProductResponseDTO getProductDetails(Long productId, Long superAdminId);


    ProductResponseDTO getProductByChannel(Long productId, Long channelId, Long companyId);
}
