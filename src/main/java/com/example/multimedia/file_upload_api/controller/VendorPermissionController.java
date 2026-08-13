package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.VendorPermissionRequestDto;
import com.example.multimedia.file_upload_api.dto.VendorPermissionResponseDto;
import com.example.multimedia.file_upload_api.service.VendorPermissionService;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor-permissions")
public class VendorPermissionController {

    @Autowired
    private VendorPermissionService vendorPermissionService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @PostMapping("/save")
    public ResponseEntity<ServiceResponse> saveVendorPermissions(@RequestBody VendorPermissionRequestDto requestDto) {
        ServiceResponse response = new ServiceResponse();
        try {
            vendorPermissionService.saveVendorPermissions(requestDto);
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, "200", "Permissions saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, "500", e.getMessage()));
        }
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<ServiceResponse> getVendorPermissions(@PathVariable Long companyId) {
        ServiceResponse response = new ServiceResponse();
        try {
            VendorPermissionResponseDto data = vendorPermissionService.getVendorPermissions(companyId);
            response.addData("result", data);
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, "200", "Permissions fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, "500", e.getMessage()));
        }
    }

    @GetMapping("/my-permissions")
    public ResponseEntity<ServiceResponse> getMyPermissions() {
        ServiceResponse response = new ServiceResponse();
        try {
            VendorPermissionResponseDto data = vendorPermissionService.getMyPermissions();
            response.addData("result", data);
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, "200", "Your permissions fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, "500", e.getMessage()));
        }
    }
}
