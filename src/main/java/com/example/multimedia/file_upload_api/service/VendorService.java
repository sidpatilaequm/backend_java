package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.CompanyDetailsDTO;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorService {

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private VendorMasterRepository vendorMasterRepository;

    @Autowired
    private SupplierRegistrationRepository supplierRegistrationRepository;

    @Autowired
    private SupplierRegistrationDocumentTypeRepository documentTypeSelectionRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Transactional
    public ServiceResponse getAllVendors() {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current admin ID for filtering — an admin/PROC_MGR IS a SuperAdmin row, but an
            // employee (EMPLOYEE/PURCHASE_DEPT/SUBMITTER/APPROVER) is only a UserDetail row linked
            // to one, so getCurrentSuperAdminId() alone (SuperAdmin-table lookup only) threw for
            // every non-admin caller — this is what made the employee Vendor List screen silently
            // come back empty. Same isCurrentUserSuperAdmin()-branch pattern already used in
            // PurchaseRequisitionServiceImpl for the same reason.
            Long currentAdminId;
            if (currentUserService.isCurrentUserSuperAdmin()) {
                currentAdminId = currentUserService.getCurrentSuperAdminId();
            } else {
                UserDetail currentUser = currentUserService.getCurrentUser();
                if (currentUser.getSuperAdmin() == null) {
                    throw new RuntimeException("Current user is not linked to an organisation.");
                }
                currentAdminId = currentUser.getSuperAdmin().getSuperAdminId();
            }

            // Get all VendorMaster records
            List<VendorMaster> vendorMasters = vendorMasterRepository.findAll();
            System.out.println("Found vendor masters: " + vendorMasters.size());

            // Prepare response data
            List<Map<String, Object>> vendorList = new ArrayList<>();
            for (VendorMaster vm : vendorMasters) {
                // company_details is the live source of truth for a vendor's profile now (V9
                // migration), reached via the real FK on VendorMaster — a vendor with no link
                // (predates the migration, never backfilled) has nothing reliable to show and is
                // skipped, same as the old supplierRegistration-null skip did. Unlike that old
                // check, this one no longer depends on supplier_registration_id also being set —
                // exactly the gap that made an approved vendor invisible here before.
                CompanyDetails company = vm.getCompanyDetails();
                if (company == null) {
                    continue;
                }

                UserDetail user = company.getUser();
                if (user == null) {
                    continue; // Skip if no user linked
                }

                // Ensure this user belongs to the current super admin
                if (user.getSuperAdmin() == null || !user.getSuperAdmin().getSuperAdminId().equals(currentAdminId)) {
                    continue;
                }

                Map<String, Object> vendorData = new HashMap<>();

                // User details
                vendorData.put("userId", user.getUserId());
                vendorData.put("email", user.getEmail());
                vendorData.put("firstName", user.getFirstName());
                vendorData.put("lastName", user.getLastName());
                vendorData.put("phoneNumber", user.getPhoneNumber());
                vendorData.put("isActive", user.getIsActive());

                vendorData.put("companyId", company.getCompanyId());

                // Vendor Master + CompanyDetails details
                vendorData.put("vendorId", vm.getVendorId());
                SupplierRegistration reg = company.getSupplierRegistration();
                vendorData.put("registrationId", reg != null ? reg.getId() : null);
                vendorData.put("bpNo", vm.getBpNo());
                vendorData.put("companyName", company.getCompanyName()); // mapping name to companyName to keep API compatible
                vendorData.put("name", company.getCompanyName());
                vendorData.put("gstNumber", company.getGstinNumber());
                vendorData.put("pan", company.getPanNumber());
                // No dedicated city field on CompanyDetails (just a free-text address) —
                // cityName was only ever populated for SAP-imported vendors anyway; the frontend
                // already falls back to "Location not specified" when it's absent.

                // Product/Service/Scheduling agreement/Sub-contracting, kept live here (dual-
                // written by SupplierRegistrationService.setVendorBusinessTypes). Superseded by
                // documentTypeSelections below for registrations decided under the newer
                // per-company-code flow, but left populated for whichever old registrations
                // still only have this.
                vendorData.put("vendorCategory", company.getVendorCategory());
                vendorData.put("vendorTypeProduct", company.isVendorTypeProduct());
                vendorData.put("vendorTypeService", company.isVendorTypeService());
                vendorData.put("vendorTypeSubcontracting", company.isVendorTypeSubcontracting());
                vendorData.put("vendorTypeSchedulingAgreement", company.isVendorTypeSchedulingAgreement());

                // Per-company document type codes (e.g. company 1000 -> NB, ZNB, ZCAP) the
                // approver actually granted this vendor — see AdminWorkflows' Document Types
                // picker and SupplierRegistrationService.setVendorDocumentTypes. Still keyed by
                // the original registration id — this grant table is unrelated to this migration.
                List<SupplierRegistrationDocumentType> docTypeSelections = reg != null
                        ? documentTypeSelectionRepository.findByRegistrationId(reg.getId())
                        : List.of();
                Map<String, String> classificationByCode = documentTypeRepository.findAllById(
                        docTypeSelections.stream().map(SupplierRegistrationDocumentType::getDocTypeCode).distinct().toList()
                ).stream().collect(Collectors.toMap(DocumentType::getCode, DocumentType::getClassification));
                vendorData.put("documentTypeSelections", docTypeSelections.stream()
                        .map(s -> {
                            Map<String, String> m = new HashMap<>();
                            m.put("companyCode", s.getCompanyCode());
                            m.put("docTypeCode", s.getDocTypeCode());
                            m.put("classification", classificationByCode.get(s.getDocTypeCode()));
                            return m;
                        })
                        .toList());

                vendorList.add(vendorData);
            }

            response.addData("vendors", vendorList);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response,
                    AppConstants.SUCCESSCODE,
                    "Vendors retrieved successfully");

        } catch (Exception e) {
            e.printStackTrace(); // Add stack trace for debugging
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Failed to retrieve vendors: " + e.getMessage());
        }
    }

    @Transactional
    public ServiceResponse updateVendorDetails(Long userId, CompanyDetailsDTO dto) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get user details
            UserDetail user = userDetailRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get user authentication
            UserAuthentication userAuth = userAuthenticationRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User authentication not found"));

            // If authKey is not provided, get it from user authentication
            String authKey = dto.getAuthKey();
            if (authKey == null || authKey.isEmpty()) {
                authKey = userAuth.getAuthKey();
            }

            // Get company details
            CompanyDetails company = companyDetailsRepository.findByUserUserId(userId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Company details not found"));

            // Update company details
            if (dto.getAuthKey() != null)
                company.setAuthKey(dto.getAuthKey());

            // Save updated company details
            company = companyDetailsRepository.save(company);

            // Prepare response
            Map<String, Object> vendorData = new HashMap<>();
            vendorData.put("companyId", company.getCompanyId());
            vendorData.put("authKey", company.getAuthKey());

            response.addData("vendor", vendorData);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response,
                    AppConstants.SUCCESSCODE,
                    "Vendor details updated successfully");

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Failed to update vendor details: " + e.getMessage());
        }
    }

    public java.util.Map<String, Object> getVendorById(Long id) {
        VendorMaster vm = vendorMasterRepository.findById(id).orElse(null);
        if (vm == null) return null;
        // company_details first (V9 migration), falling back to the supplierRegistration link
        // only for a vendor that predates the migration and hasn't been backfilled.
        String name = vm.getCompanyDetails() != null ? vm.getCompanyDetails().getCompanyName()
                : (vm.getSupplierRegistration() != null ? vm.getSupplierRegistration().getVendorName() : null);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("vendorId", vm.getVendorId());
        data.put("bp_no", vm.getBpNo());
        data.put("bpNo", vm.getBpNo());
        data.put("name", name);
        data.put("companyName", name);
        return data;
    }

    public List<CompanyDetails> getVendors(Long userId) {
        // Get the vendor authorization
        Authorization vendorAuth = authorizationRepository.findByAuthKey("vendor")
                .orElseThrow(() -> new RuntimeException("Vendor authorization not found"));

        // Get all user authentications with vendor role
        List<UserAuthentication> vendorAuthentications = userAuthenticationRepository
                .findByAuthKey(vendorAuth.getAuthKey());

        // Get all user IDs with vendor role
        List<Long> vendorUserIds = vendorAuthentications.stream()
                .map(UserAuthentication::getUserId)
                .collect(Collectors.toList());

        // Get all company details for these users
        return companyDetailsRepository.findByUserUserIdIn(vendorUserIds);
    }
}
