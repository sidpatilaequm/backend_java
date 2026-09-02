package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.AsnRequestDto;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.service.AsnService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor/asns")
public class AsnController {

    @Autowired
    private AsnService asnService;

    @Autowired
    private com.example.multimedia.file_upload_api.util.SecurityContextUtils securityContextUtils;

    @PostMapping(value = "", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ServiceResponse> createAsn(
            @RequestPart("asnData") String asnDataJson,
            HttpServletRequest request) {
        
        try {
            // Extract the user ID from headers (e.g. injected by API Gateway or Spring Security filter)
            String userIdStr = request.getHeader("X-User-Id");
            if (userIdStr == null) {
                // Try from another standard header or parameter if needed, returning error for now
                ServiceResponse response = new ServiceResponse();
                response.setStatus("ERROR");
                response.setStatusMsg("Unauthorized: X-User-Id missing");
                return ResponseEntity.status(401).body(response);
            }
            Long userId = Long.parseLong(userIdStr);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            AsnRequestDto asnRequestDto = mapper.readValue(asnDataJson, AsnRequestDto.class);

            // Get all files from MultipartHttpServletRequest
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

            ServiceResponse serviceResponse = asnService.createAsn(asnRequestDto, fileMap, userId);

            if ("SUCCESS".equalsIgnoreCase(serviceResponse.getStatus()) || "200".equals(serviceResponse.getStatus())) {
                return ResponseEntity.ok(serviceResponse);
            } else {
                return ResponseEntity.badRequest().body(serviceResponse);
            }
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error creating ASN: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("")
    public ResponseEntity<ServiceResponse> getAsns(
            @RequestParam(value = "vendorBpno", required = false) String vendorBpno,
            @RequestParam(value = "company_code", required = false) String companyCode) {
        try {
            ServiceResponse serviceResponse;
            if (vendorBpno != null && !vendorBpno.trim().isEmpty()) {
                serviceResponse = asnService.getAsnsByVendorBpno(vendorBpno, companyCode);
            } else {
                Long vendorId = null;
                try {
                    vendorId = securityContextUtils.getCurrentVendorId();
                } catch (Exception e) {
                    // Ignore if not authenticated as vendor
                }
                
                if (vendorId != null) {
                    serviceResponse = asnService.getAsnsByVendorId(vendorId, companyCode);
                } else {
                    serviceResponse = asnService.getAllAsns();
                }
            }

            if ("SUCCESS".equalsIgnoreCase(serviceResponse.getStatus()) || "200".equals(serviceResponse.getStatus())) {
                return ResponseEntity.ok(serviceResponse);
            } else {
                return ResponseEntity.badRequest().body(serviceResponse);
            }
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error fetching ASNs: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{asnNumber}")
    public ResponseEntity<ServiceResponse> getAsnById(@PathVariable String asnNumber) {
        try {
            ServiceResponse serviceResponse = asnService.getAsnById(asnNumber);
            if ("SUCCESS".equalsIgnoreCase(serviceResponse.getStatus()) || "200".equals(serviceResponse.getStatus())) {
                return ResponseEntity.ok(serviceResponse);
            } else {
                return ResponseEntity.status(404).body(serviceResponse);
            }
        } catch (Exception e) {
            ServiceResponse errorResponse = new ServiceResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setStatusMsg("Error fetching ASN details: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
