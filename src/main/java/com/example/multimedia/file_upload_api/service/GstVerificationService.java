package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.GstSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GstVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(GstVerificationService.class);

    @Autowired
    private GSTOCRService gstOcrService;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    public ServiceResponse verifyGst(MultipartFile file, String gstin, Long companyId) {
        ServiceResponse response = new ServiceResponse();
        try {
            JSONObject gstResult;
            if (file != null && !file.isEmpty()) {
                gstResult = gstOcrService.processGSTFile(file);
            } else if (gstin != null && !gstin.trim().isEmpty()) {
                gstResult = getMockSearchResponse(gstin);
            } else {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "Either GST file or GSTIN number must be provided.");
            }

            JSONObject gstData = gstResult.optJSONObject("data");
            if (gstData == null) {
                if (gstResult.has("Legal Trade Name")) {
                    gstData = gstResult;
                } else {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(
                            response, AppConstants.ERRORCODE, "Failed to parse GST verification details.");
                }
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("gstinNumber", gstData.optString("gstin", gstData.optString("GSTIN Number", gstin)));
            dataMap.put("legalTradeName", gstData.optString("legalName", gstData.optString("Legal Trade Name")));
            dataMap.put("companyName", gstData.optString("tradeName", gstData.optString("Legal Trade Name")));
            dataMap.put("typeOfRegistration", gstData.optString("type", gstData.optString("Registration Type", "Regular")));
            dataMap.put("dateOfRegistration", gstData.optString("registrationDate", gstData.optString("Registration Date")));

            Object addressObj = gstData.opt("address");
            if (addressObj instanceof JSONObject) {
                JSONObject addr = (JSONObject) addressObj;
                String formattedAddr = String.format("%s, %s, %s, %s - %s",
                        addr.optString("building"), addr.optString("street"),
                        addr.optString("city"), addr.optString("state"), addr.optString("pincode"));
                dataMap.put("registeredAddress", formattedAddr);
            } else if (addressObj instanceof String) {
                dataMap.put("registeredAddress", addressObj);
            } else {
                dataMap.put("registeredAddress", gstData.optString("Registered Address", "Address to be updated"));
            }

            response.addData("gstDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "GST details verified successfully (unsubmitted)");

        } catch (Exception e) {
            logger.error("Error verifying GST Details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "GST Verification error: " + e.getMessage());
        }
    }

    public ServiceResponse submitGst(GstSubmitDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Always resolve the current user from the JWT token
            UserDetail currentUser = currentUserService.getCurrentUser();
            Long currentUserId = currentUser.getUserId();

            CompanyDetails company = null;

            // Check if this user already has a CompanyDetails row scoped to them
            List<CompanyDetails> existingByUser = companyDetailsRepository.findByUserUserId(currentUserId);
            if (!existingByUser.isEmpty()) {
                // Update the user's own existing company record
                company = existingByUser.get(0);
            } else if (currentUser.getCompany() != null) {
                // User is linked to a shared company — create a new dedicated record
                // to avoid overwriting another user's GST data
                company = new CompanyDetails();
                company.setCompanyCode("COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                company.setSuperAdmin(currentUser.getSuperAdmin());
            } else {
                // No company at all — create a fresh one
                company = new CompanyDetails();
                company.setCompanyCode("COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                company.setSuperAdmin(currentUser.getSuperAdmin());
            }

            // Always stamp the current user onto this company record
            company.setUser(currentUser);

            company.setGstinNumber(dto.getGstinNumber());
            company.setLegalTradeName(dto.getLegalTradeName());
            company.setCompanyName(dto.getCompanyName());
            company.setRegisteredAddress(dto.getRegisteredAddress());
            company.setTypeOfRegistration(dto.getTypeOfRegistration());
            
            if (dto.getDateOfRegistration() != null && !dto.getDateOfRegistration().trim().isEmpty()) {
                try {
                    company.setDateOfRegistration(LocalDate.parse(dto.getDateOfRegistration(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } catch (Exception e) {
                    try {
                        company.setDateOfRegistration(LocalDate.parse(dto.getDateOfRegistration(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    } catch (Exception ex) {
                        company.setDateOfRegistration(LocalDate.now());
                    }
                }
            } else {
                company.setDateOfRegistration(LocalDate.now());
            }

            if (company.getCompanyCode() == null || company.getCompanyCode().trim().isEmpty()) {
                company.setCompanyCode(company.getGstinNumber() != null ? company.getGstinNumber() : 
                        "COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }

            company = companyDetailsRepository.save(company);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("companyId", company.getCompanyId());
            dataMap.put("gstinNumber", company.getGstinNumber());
            dataMap.put("legalTradeName", company.getLegalTradeName());

            response.addData("gstDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "GST details submitted and saved successfully");
        } catch (Exception e) {
            logger.error("Error submitting GST Details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "GST submission error: " + e.getMessage());
        }
    }

    public ServiceResponse getGstDetails() {
        ServiceResponse response = new ServiceResponse();
        try {
            // Resolve company by userId from the JWT token — NOT by user.getCompany()
            // user.getCompany() can return a shared/stale company from a previous user
            Long currentUserId = currentUserService.getCurrentUserId();
            List<CompanyDetails> companies = companyDetailsRepository.findByUserUserId(currentUserId);

            if (companies.isEmpty()) {
                // This user has never submitted GST — return empty/null fields cleanly
                Map<String, Object> emptyMap = new HashMap<>();
                emptyMap.put("gstinNumber", null);
                emptyMap.put("legalTradeName", null);
                emptyMap.put("companyName", null);
                emptyMap.put("typeOfRegistration", null);
                emptyMap.put("dateOfRegistration", null);
                emptyMap.put("registeredAddress", null);
                response.addData("gstDetails", emptyMap);
                return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                        response, AppConstants.SUCCESSCODE, "No GST details submitted yet for this user.");
            }

            CompanyDetails company = companies.get(0);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("gstinNumber", company.getGstinNumber());
            dataMap.put("legalTradeName", company.getLegalTradeName());
            dataMap.put("companyName", company.getCompanyName());
            dataMap.put("typeOfRegistration", company.getTypeOfRegistration());
            dataMap.put("dateOfRegistration", company.getDateOfRegistration());
            dataMap.put("registeredAddress", company.getRegisteredAddress());

            response.addData("gstDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "GST details retrieved successfully");
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Failed to retrieve GST details: " + e.getMessage());
        }
    }

    private JSONObject getMockSearchResponse(String gstin) {
        JSONObject res = new JSONObject();
        res.put("status", "success");
        JSONObject data = new JSONObject();
        data.put("gstin", gstin);
        data.put("legalName", "ACME INFO PRIVATE LIMITED");
        data.put("tradeName", "ACME INFO");
        data.put("type", "Regular");
        data.put("registrationDate", "05/10/2024");
        data.put("Registered Address", "10th Floor, 1006, Road, Ahmedabad, B Square-1, Gujarat, Near Neptune House, 380058 Ambli Brts,");
        res.put("data", data);
        return res;
    }
}
