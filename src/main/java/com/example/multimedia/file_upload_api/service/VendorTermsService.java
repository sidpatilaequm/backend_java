package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.VendorTermsDTO;
import com.example.multimedia.file_upload_api.dto.VendorTermsResponseDTO;
import com.example.multimedia.file_upload_api.entity.VendorTerms;
import com.example.multimedia.file_upload_api.repository.VendorTermsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorTermsService {

    @Autowired
    private VendorTermsRepository vendorTermsRepository;

    public VendorTermsResponseDTO createVendorTerms(VendorTermsDTO dto) {
        VendorTerms vendorTerms = new VendorTerms();
        updateVendorTermsFromDTO(vendorTerms, dto);
        vendorTerms = vendorTermsRepository.save(vendorTerms);
        return convertToResponseDTO(vendorTerms);
    }

    public VendorTermsResponseDTO getVendorTerms(Long id, Long callerSuperAdminId) {
        VendorTerms vendorTerms = findOwned(id, callerSuperAdminId);
        return convertToResponseDTO(vendorTerms);
    }

    // None of the by-id operations below checked that the record actually belonged to the calling
    // super admin's own tenant — any super admin could view/edit/delete/download another tenant's
    // vendor terms documents by guessing a sequential id. Centralized here since every one of them
    // needs the identical check.
    private VendorTerms findOwned(Long id, Long callerSuperAdminId) {
        VendorTerms vendorTerms = vendorTermsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor terms not found"));
        Long ownerSuperAdminId = vendorTerms.getUser() != null && vendorTerms.getUser().getSuperAdmin() != null
                ? vendorTerms.getUser().getSuperAdmin().getSuperAdminId() : null;
        if (ownerSuperAdminId == null || !ownerSuperAdminId.equals(callerSuperAdminId)) {
            throw new SecurityException("Vendor terms not found");
        }
        return vendorTerms;
    }

    public List<VendorTermsResponseDTO> getVendorTermsByUser(Long userId) {
        return vendorTermsRepository.findByUser_UserId(userId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<VendorTermsResponseDTO> getVendorTermsByCompany(Long companyId) {
        return vendorTermsRepository.findByCompany_CompanyId(companyId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public VendorTermsResponseDTO updateVendorTerms(Long id, VendorTermsDTO dto, Long callerSuperAdminId) {
        VendorTerms vendorTerms = findOwned(id, callerSuperAdminId);
        updateVendorTermsFromDTO(vendorTerms, dto);
        vendorTerms = vendorTermsRepository.save(vendorTerms);
        return convertToResponseDTO(vendorTerms);
    }

    public void deleteVendorTerms(Long id, Long callerSuperAdminId) {
        findOwned(id, callerSuperAdminId);
        vendorTermsRepository.deleteById(id);
    }

    public ResponseEntity<byte[]> getPaymentTermsFile(Long id, Long callerSuperAdminId) {
        VendorTerms vendorTerms = findOwned(id, callerSuperAdminId);
        return createFileResponse(vendorTerms.getPaymentTermsFile(),
                                vendorTerms.getPaymentTermsFileName(),
                                vendorTerms.getPaymentTermsFileType());
    }

    public ResponseEntity<byte[]> getIncotermsFile(Long id, Long callerSuperAdminId) {
        VendorTerms vendorTerms = findOwned(id, callerSuperAdminId);
        return createFileResponse(vendorTerms.getIncotermsFile(),
                                vendorTerms.getIncotermsFileName(),
                                vendorTerms.getIncotermsFileType());
    }

    public ResponseEntity<byte[]> getDeliveryTermsFile(Long id, Long callerSuperAdminId) {
        VendorTerms vendorTerms = findOwned(id, callerSuperAdminId);
        return createFileResponse(vendorTerms.getDeliveryTermsFile(),
                                vendorTerms.getDeliveryTermsFileName(),
                                vendorTerms.getDeliveryTermsFileType());
    }

    private void updateVendorTermsFromDTO(VendorTerms vendorTerms, VendorTermsDTO dto) {
        try {
            if (dto.getPaymentTermsFile() != null) {
                vendorTerms.setPaymentTermsFile(dto.getPaymentTermsFile().getBytes());
                vendorTerms.setPaymentTermsFileName(dto.getPaymentTermsFile().getOriginalFilename());
                vendorTerms.setPaymentTermsFileType(dto.getPaymentTermsFile().getContentType());
            }
            if (dto.getIncotermsFile() != null) {
                vendorTerms.setIncotermsFile(dto.getIncotermsFile().getBytes());
                vendorTerms.setIncotermsFileName(dto.getIncotermsFile().getOriginalFilename());
                vendorTerms.setIncotermsFileType(dto.getIncotermsFile().getContentType());
            }
            if (dto.getDeliveryTermsFile() != null) {
                vendorTerms.setDeliveryTermsFile(dto.getDeliveryTermsFile().getBytes());
                vendorTerms.setDeliveryTermsFileName(dto.getDeliveryTermsFile().getOriginalFilename());
                vendorTerms.setDeliveryTermsFileType(dto.getDeliveryTermsFile().getContentType());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing file upload", e);
        }
    }

    private VendorTermsResponseDTO convertToResponseDTO(VendorTerms vendorTerms) {
        VendorTermsResponseDTO dto = new VendorTermsResponseDTO();
        dto.setVendorTermsId(vendorTerms.getVendorTermsId());
        dto.setUserId(vendorTerms.getUser().getUserId());
        dto.setCompanyId(vendorTerms.getCompany().getCompanyId());
        dto.setIsActive(vendorTerms.getIsActive());
        dto.setPaymentTermsFileName(vendorTerms.getPaymentTermsFileName());
        dto.setPaymentTermsFileType(vendorTerms.getPaymentTermsFileType());
        dto.setIncotermsFileName(vendorTerms.getIncotermsFileName());
        dto.setIncotermsFileType(vendorTerms.getIncotermsFileType());
        dto.setDeliveryTermsFileName(vendorTerms.getDeliveryTermsFileName());
        dto.setDeliveryTermsFileType(vendorTerms.getDeliveryTermsFileType());
        dto.setCreatedDate(vendorTerms.getCreatedDate());
        dto.setModifiedDate(vendorTerms.getModifiedDate());
        return dto;
    }

    private ResponseEntity<byte[]> createFileResponse(byte[] fileData, String fileName, String fileType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fileType));
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
} 