package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.AsnRequestDto;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface AsnService {
    ServiceResponse createAsn(AsnRequestDto asnRequestDto,
                              Map<String, MultipartFile> files,
                              Long userId);

    ServiceResponse getAsnsByVendorBpno(String vendorBpno);

    ServiceResponse getAllAsns();

    ServiceResponse getAsnsByPoNumber(String poNumber);

    ServiceResponse getAsnById(String asnNumber);
}
