package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.materialinward.MaterialInwardDetailDto;
import com.example.multimedia.file_upload_api.dto.materialinward.MaterialInwardQueueDto;
import com.example.multimedia.file_upload_api.dto.materialinward.MaterialInwardSubmitDto;
import com.example.multimedia.file_upload_api.entity.GoodsReceipt;

import java.util.List;

public interface MaterialInwardService {
    List<MaterialInwardQueueDto> getQueue();
    MaterialInwardDetailDto getDetails(Long gateEntryId);
    GoodsReceipt submitVerification(Long gateEntryId, MaterialInwardSubmitDto dto);
}
