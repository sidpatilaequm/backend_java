package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.PanSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.PanDetails;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.PanDetailsRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PanVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(PanVerificationService.class);

    @Autowired
    private PanOcrService panOcrService;

    @Autowired
    private PanDetailsRepository panDetailsRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    public ServiceResponse verifyPan(MultipartFile file, String panNumber, Long companyId) {
        ServiceResponse response = new ServiceResponse();
        try {
            JSONObject panResult;
            if (file != null && !file.isEmpty()) {
                panResult = panOcrService.processPanFile(file);
            } else if (panNumber != null && !panNumber.trim().isEmpty()) {
                panResult = getMockPanResponse(panNumber);
            } else {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "Either PAN file or PAN number must be provided.");
            }

            JSONObject panData = panResult.optJSONObject("data");
            if (panData == null) {
                if (panResult.has("Name")) {
                    panData = panResult;
                } else {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(
                            response, AppConstants.ERRORCODE, "Failed to parse PAN verification details.");
                }
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("panNumber", panData.optString("pan", panData.optString("PAN Number", panNumber)));
            dataMap.put("name", panData.optString("name", panData.optString("Name")));
            dataMap.put("dateOfBirthIncorporation", panData.optString("dateOfBirthIncorporation", panData.optString("Date of Birth/Incorporation")));
            dataMap.put("category", panData.optString("category", panData.optString("Category", "Company")));
            dataMap.put("fathersName", panData.optString("fatherName", panData.optString("Father's Name", "Not Available")));

            response.addData("panDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "PAN details verified successfully (unsubmitted)");

        } catch (Exception e) {
            logger.error("Error verifying PAN Details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "PAN Verification error: " + e.getMessage());
        }
    }

    public ServiceResponse submitPan(PanSubmitDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Always resolve current user from JWT token
            UserDetail currentUser = currentUserService.getCurrentUser();
            Long currentUserId = currentUser.getUserId();

            CompanyDetails company = null;
            List<CompanyDetails> existingByUser = companyDetailsRepository.findByUserUserId(currentUserId);
            if (!existingByUser.isEmpty()) {
                company = existingByUser.get(0);
            } else {
                // No company scoped to this user yet — create one
                company = new CompanyDetails();
                company.setCompanyCode("COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                company.setSuperAdmin(currentUser.getSuperAdmin());
            }
            // Stamp the current user onto this company record
            company.setUser(currentUser);
            company = companyDetailsRepository.save(company);

            PanDetails panDetails = panDetailsRepository.findByCompanyCompanyId(company.getCompanyId())
                    .orElse(new PanDetails());
            panDetails.setCompany(company);
            panDetails.setPanNumber(dto.getPanNumber());
            panDetails.setName(dto.getName());
            panDetails.setDateOfBirthIncorporation(dto.getDateOfBirthIncorporation());
            panDetails.setCategory(dto.getCategory());
            panDetails.setFathersName(dto.getFathersName());

            panDetails = panDetailsRepository.save(panDetails);

            // Sync primary PAN number on company
            company.setPanNumber(dto.getPanNumber());
            companyDetailsRepository.save(company);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("panDetailsId", panDetails.getPanDetailsId());
            dataMap.put("panNumber", panDetails.getPanNumber());
            dataMap.put("name", panDetails.getName());

            response.addData("panDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "PAN details submitted and saved successfully");
        } catch (Exception e) {
            logger.error("Error submitting PAN Details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "PAN submission error: " + e.getMessage());
        }
    }

    public ServiceResponse getPanDetails() {
        ServiceResponse response = new ServiceResponse();
        try {
            // Resolve company by userId from token — NOT user.getCompany()
            Long currentUserId = currentUserService.getCurrentUserId();
            List<CompanyDetails> companies = companyDetailsRepository.findByUserUserId(currentUserId);

            Map<String, Object> dataMap = null;
            if (!companies.isEmpty()) {
                CompanyDetails company = companies.get(0);
                PanDetails pan = company.getPanDetails();
                if (pan != null) {
                    dataMap = new HashMap<>();
                    dataMap.put("panNumber", pan.getPanNumber());
                    dataMap.put("name", pan.getName());
                    dataMap.put("dateOfBirthIncorporation", pan.getDateOfBirthIncorporation());
                    dataMap.put("category", pan.getCategory());
                    dataMap.put("fathersName", pan.getFathersName());
                }
            }
            response.addData("panDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, dataMap == null
                            ? "No PAN details submitted yet for this user."
                            : "PAN details retrieved successfully");
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Failed to retrieve PAN details: " + e.getMessage());
        }
    }

    private JSONObject getMockPanResponse(String panNumber) {
        JSONObject res = new JSONObject();
        res.put("status", "success");
        JSONObject data = new JSONObject();
        data.put("pan", panNumber);
        data.put("name", "ACME INFO PRIVATE LIMITED");
        data.put("dateOfBirthIncorporation", "13/07/2017");
        data.put("category", "BUSINESS");
        data.put("fatherName", "Not Available");
        res.put("data", data);
        return res;
    }
}
