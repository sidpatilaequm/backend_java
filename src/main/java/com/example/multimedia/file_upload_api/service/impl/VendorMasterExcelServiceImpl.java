package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.UploadResponse;
import com.example.multimedia.file_upload_api.dto.VendorMasterDto;
import com.example.multimedia.file_upload_api.entity.VendorMaster;
import com.example.multimedia.file_upload_api.repository.VendorMasterRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.service.VendorMasterExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorMasterExcelServiceImpl implements VendorMasterExcelService {

    private final VendorMasterRepository vendorMasterRepository;
    private final UserDetailRepository userDetailRepository;
    private final SuperAdminRepository superAdminRepository;
    private final CompanyDetailsRepository companyDetailsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UploadResponse uploadVendorMaster(VendorMasterDto dto) {
        if (dto == null || dto.getBpNo() == null) {
            throw new IllegalArgumentException("Invalid SAP vendor data");
        }

        int inserted = 0;
        int updated = 0;

        java.util.List<com.example.multimedia.file_upload_api.entity.SuperAdmin> admins = superAdminRepository.findAll();

        VendorMaster vendorMaster = vendorMasterRepository.findByBpNo(dto.getBpNo()).orElse(null);
        if (vendorMaster == null) {
            vendorMaster = new VendorMaster();
            inserted = 1;
        } else {
            updated = 1;
        }

        // Name/GST/bank/address no longer live on VendorMaster itself — this bulk-import path has
        // no SupplierRegistration to link to (these vendors never went through Become-a-Supplier),
        // so vendorMaster.supplierRegistration stays null and those details live only on the
        // CompanyDetails record created below, same as everywhere else that reads a SAP-imported
        // vendor's details.
        vendorMaster.setBpNo(dto.getBpNo());
        if (vendorMaster.getSuperAdmin() == null && !admins.isEmpty()) {
            vendorMaster.setSuperAdmin(admins.get(0));
        }

        vendorMasterRepository.save(vendorMaster);

        // Auto-create UserDetails if it doesn't exist
        if (dto.getEmailAddress() != null && !dto.getEmailAddress().isEmpty()) {
            boolean userExists = userDetailRepository.existsByEmail(dto.getEmailAddress());
            if (!userExists) {
                com.example.multimedia.file_upload_api.entity.UserDetail user = new com.example.multimedia.file_upload_api.entity.UserDetail();
                user.setEmail(dto.getEmailAddress());
                user.setFirstName(dto.getVendorName());
                user.setUserType(com.example.multimedia.file_upload_api.enums.UserType.VENDOR);
                user.setIsActive(true);
                user.setPassword(passwordEncoder.encode("User@123"));

                if (!admins.isEmpty()) {
                    user.setSuperAdmin(admins.get(0));
                }

                user = userDetailRepository.save(user);

                // Auto-create CompanyDetails which is required for PR and PO workflows
                com.example.multimedia.file_upload_api.entity.CompanyDetails company = new com.example.multimedia.file_upload_api.entity.CompanyDetails();
                company.setCompanyName(dto.getVendorName());
                company.setCompanyCode(dto.getBpNo());
                company.setGstinNumber(dto.getGstNumber());
                company.setStatus("ACTIVE");
                company.setAuthKey("vendor");
                if (!admins.isEmpty()) {
                    company.setSuperAdmin(admins.get(0));
                }
                company.setUser(user);
                company = companyDetailsRepository.save(company);

                user.setCompany(company);
                userDetailRepository.save(user);
            } else {
                // Update name if email matches as per requirements
                com.example.multimedia.file_upload_api.entity.UserDetail user = userDetailRepository.findByEmail(dto.getEmailAddress()).orElse(null);
                if (user != null) {
                    user.setFirstName(dto.getVendorName());
                    userDetailRepository.save(user);
                }
            }
        }

        return UploadResponse.builder()
                .status("SUCCESS")
                .totalRows(1)
                .inserted(inserted)
                .updated(updated)
                .failed(0)
                .build();
    }
}
