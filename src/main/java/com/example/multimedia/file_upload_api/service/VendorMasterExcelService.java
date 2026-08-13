package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.UploadResponse;
import com.example.multimedia.file_upload_api.dto.VendorMasterDto;
import java.io.IOException;

public interface VendorMasterExcelService {
    UploadResponse uploadVendorMaster(VendorMasterDto dto) throws IOException;
}
