package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.MsmeSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.MsmeDetails;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.MsmeDetailsRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MsmeVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(MsmeVerificationService.class);
    private static final String MSME_URL = "https://api.attestr.com/api/v1/public/corpx/udyam";

    @Autowired
    private MsmeDetailsRepository msmeDetailsRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MockResponseService mockResponseService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${attestr.auth.token}")
    private String authToken;

    public ServiceResponse verifyMsme(String udyamNumber, Long companyId) {
        ServiceResponse response = new ServiceResponse();
        try {
            JSONObject msmeResult;
            if (mockResponseService.isUseMockResponses()) {
                logger.info("Using mock response for MSME verification");
                msmeResult = mockResponseService.getMockResponse("msme");
            } else {
                logger.info("Calling Attestr MSME verification API for udyam number: {}", udyamNumber);
                msmeResult = callAttestrMsmeApi(udyamNumber);
            }

            if (!msmeResult.optBoolean("valid", false)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, msmeResult.optString("error", "MSME Verification failed. Udyam number invalid."));
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("udyamNumber", udyamNumber);
            dataMap.put("entityName", msmeResult.optString("entity"));
            dataMap.put("type", msmeResult.optString("type"));
            
            Object majorActivityObj = msmeResult.opt("majorActivity");
            if (majorActivityObj instanceof JSONArray) {
                dataMap.put("majorActivity", majorActivityObj.toString());
            } else if (majorActivityObj != null) {
                dataMap.put("majorActivity", majorActivityObj.toString());
            }
            
            dataMap.put("gender", msmeResult.optString("gender"));
            dataMap.put("socialCategory", msmeResult.optString("socialCategory"));
            dataMap.put("incorporatedDate", msmeResult.optString("incorporated"));
            dataMap.put("commencedDate", msmeResult.optString("commenced"));
            dataMap.put("registeredDate", msmeResult.optString("registered"));
            
            dataMap.put("classifications", msmeResult.optJSONArray("classifications") != null ? 
                    msmeResult.optJSONArray("classifications").toString() : null);
            dataMap.put("locations", msmeResult.optJSONArray("locations") != null ? 
                    msmeResult.optJSONArray("locations").toString() : null);
            dataMap.put("officialAddress", msmeResult.optJSONObject("officialAddress") != null ? 
                    msmeResult.optJSONObject("officialAddress").toString() : null);
            dataMap.put("nicCodes", msmeResult.optJSONArray("nicCodes") != null ? 
                    msmeResult.optJSONArray("nicCodes").toString() : null);
            
            dataMap.put("dic", msmeResult.optString("dic"));
            dataMap.put("dfo", msmeResult.optString("dfo"));

            response.addData("msmeDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "MSME details verified successfully (unsubmitted)");

        } catch (Exception e) {
            logger.error("Error verifying MSME Details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "MSME Verification error: " + e.getMessage());
        }
    }

    public ServiceResponse submitMsme(MsmeSubmitDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            UserDetail currentUser = currentUserService.getCurrentUser();
            Long currentUserId = currentUser.getUserId();

            CompanyDetails company;
            List<CompanyDetails> existingByUser = companyDetailsRepository.findByUserUserId(currentUserId);
            if (!existingByUser.isEmpty()) {
                company = existingByUser.get(0);
            } else {
                company = new CompanyDetails();
                company.setCompanyCode("COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                company.setSuperAdmin(currentUser.getSuperAdmin());
            }
            company.setUser(currentUser);
            company = companyDetailsRepository.save(company);

            MsmeDetails msmeDetails = msmeDetailsRepository.findByCompanyCompanyId(company.getCompanyId())
                    .orElse(new MsmeDetails());
            msmeDetails.setCompany(company);
            msmeDetails.setUdyamNumber(dto.getUdyamNumber());
            msmeDetails.setEntityName(dto.getEntityName());
            msmeDetails.setType(dto.getType());
            msmeDetails.setMajorActivity(dto.getMajorActivity());
            msmeDetails.setGender(dto.getGender());
            msmeDetails.setSocialCategory(dto.getSocialCategory());
            msmeDetails.setIncorporatedDate(dto.getIncorporatedDate());
            msmeDetails.setCommencedDate(dto.getCommencedDate());
            msmeDetails.setRegisteredDate(dto.getRegisteredDate());
            msmeDetails = msmeDetailsRepository.save(msmeDetails);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("msmeDetailsId", msmeDetails.getMsmeDetailsId());
            dataMap.put("udyamNumber", msmeDetails.getUdyamNumber());
            dataMap.put("entityName", msmeDetails.getEntityName());
            response.addData("msmeDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "MSME details submitted and saved successfully");
        } catch (Exception e) {
            logger.error("Error submitting MSME Details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "MSME submission error: " + e.getMessage());
        }
    }

    public ServiceResponse getMsmeDetails() {
        ServiceResponse response = new ServiceResponse();
        try {
            Long currentUserId = currentUserService.getCurrentUserId();
            List<CompanyDetails> companies = companyDetailsRepository.findByUserUserId(currentUserId);
            Map<String, Object> dataMap = null;
            if (!companies.isEmpty()) {
                MsmeDetails msme = companies.get(0).getMsmeDetails();
                if (msme != null) {
                    dataMap = new HashMap<>();
                    dataMap.put("udyamNumber", msme.getUdyamNumber());
                    dataMap.put("entityName", msme.getEntityName());
                    dataMap.put("type", msme.getType());
                    dataMap.put("majorActivity", msme.getMajorActivity());
                    dataMap.put("gender", msme.getGender());
                    dataMap.put("socialCategory", msme.getSocialCategory());
                    dataMap.put("incorporatedDate", msme.getIncorporatedDate());
                    dataMap.put("commencedDate", msme.getCommencedDate());
                    dataMap.put("registeredDate", msme.getRegisteredDate());
                }
            }
            response.addData("msmeDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, dataMap == null
                            ? "No MSME details submitted yet for this user."
                            : "MSME details retrieved successfully");
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Failed to retrieve MSME details: " + e.getMessage());
        }
    }

    private JSONObject callAttestrMsmeApi(String udyamNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + authToken);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("reg", udyamNumber);
        
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                MSME_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Attestr MSME API returned non-OK status: " + response.getStatusCode());
        }
        
        return new JSONObject(response.getBody());
    }
}
