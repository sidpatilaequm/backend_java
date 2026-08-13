package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface MaterialExcelService {
    UploadResponse uploadMaterialMaster(MultipartFile file) throws IOException;
}
