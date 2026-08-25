package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.materialinward.MaterialInwardDetailDto;
import com.example.multimedia.file_upload_api.dto.materialinward.MaterialInwardQueueDto;
import com.example.multimedia.file_upload_api.dto.materialinward.MaterialInwardSubmitDto;
import com.example.multimedia.file_upload_api.entity.GoodsReceipt;
import com.example.multimedia.file_upload_api.service.MaterialInwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/material-inward")

public class MaterialInwardController {

    @Autowired
    private MaterialInwardService materialInwardService;

    @GetMapping("/queue")
    public ResponseEntity<List<MaterialInwardQueueDto>> getQueue() {
        return ResponseEntity.ok(materialInwardService.getQueue());
    }

    @GetMapping("/{gateEntryId}")
    public ResponseEntity<MaterialInwardDetailDto> getDetails(@PathVariable Long gateEntryId) {
        return ResponseEntity.ok(materialInwardService.getDetails(gateEntryId));
    }

    @PostMapping("/{gateEntryId}/verify")
    public ResponseEntity<GoodsReceipt> submitVerification(@PathVariable Long gateEntryId, @RequestBody MaterialInwardSubmitDto dto) {
        return ResponseEntity.ok(materialInwardService.submitVerification(gateEntryId, dto));
    }
}
