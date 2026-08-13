package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ItrSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.ItrDetails;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.ItrDetailsRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ItrVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(ItrVerificationService.class);
    private static final String ITR_URL = "https://api.attestr.com/api/v2/public/corpx/itr";

    @Autowired
    private ItrDetailsRepository itrDetailsRepository;

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

    public ServiceResponse verifyItr(String pan, String birthOrIncorporatedDate, String name, Long companyId) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Always resolve company by userId from token
            Long currentUserId = currentUserService.getCurrentUserId();
            List<CompanyDetails> userCompanies = companyDetailsRepository.findByUserUserId(currentUserId);
            CompanyDetails company = userCompanies.isEmpty() ? null : userCompanies.get(0);

            String finalPan = (pan != null && !pan.trim().isEmpty()) ? pan : (company != null ? company.getPanNumber() : null);
            String finalName = (name != null && !name.trim().isEmpty()) ? name : (company != null ? company.getLegalTradeName() : null);
            if (finalName == null && company != null) {
                finalName = company.getCompanyName();
            }

            String finalDate = birthOrIncorporatedDate;
            if (finalDate == null || finalDate.trim().isEmpty()) {
                if (company != null && company.getDateOfRegistration() != null) {
                    finalDate = company.getDateOfRegistration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else if (company != null && company.getPanDetails() != null && company.getPanDetails().getDateOfBirthIncorporation() != null) {
                    finalDate = company.getPanDetails().getDateOfBirthIncorporation();
                }
            }

            if (finalPan == null || finalPan.trim().isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "PAN number is required for ITR verification.");
            }
            if (finalName == null || finalName.trim().isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "Legal name is required for ITR verification.");
            }
            if (finalDate == null || finalDate.trim().isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "Birth or Incorporation date is required for ITR verification.");
            }

            JSONObject itrResult;
            if (mockResponseService.isUseMockResponses()) {
                logger.info("Using mock response for ITR verification");
                itrResult = mockResponseService.getMockResponse("itr");
            } else {
                logger.info("Calling Attestr ITR API for PAN: {}, Name: {}, Date: {}", finalPan, finalName, finalDate);
                itrResult = callAttestrItrApi(finalPan, finalDate, finalName);
            }

            if (!itrResult.optBoolean("valid", false)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, itrResult.optString("message", "ITR details match failed or unsupported ITR type."));
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("pan", finalPan);
            dataMap.put("birthOrIncorporatedDate", finalDate);
            dataMap.put("name", finalName);
            dataMap.put("fy", itrResult.optString("fy"));
            dataMap.put("itrFiled", itrResult.optBoolean("itrFiled", false));
            dataMap.put("itrType", itrResult.optString("itrType"));
            dataMap.put("grossTurnover", itrResult.optString("grossTurnover"));
            dataMap.put("grossTurnoverFormatted", itrResult.optString("grossTurnoverFormatted"));
            dataMap.put("exportTurnover", itrResult.optString("exportTurnover"));
            dataMap.put("exportTurnoverFormatted", itrResult.optString("exportTurnoverFormatted"));
            dataMap.put("panStatus", itrResult.optString("panStatus"));

            response.addData("itrDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "ITR details verified successfully (unsubmitted)");

        } catch (Exception e) {
            logger.error("Error verifying ITR details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "ITR Verification error: " + e.getMessage());
        }
    }

    public ServiceResponse submitItr(ItrSubmitDTO dto) {
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

            ItrDetails itrDetails = itrDetailsRepository.findByCompanyCompanyId(company.getCompanyId())
                    .orElse(new ItrDetails());
            itrDetails.setCompany(company);
            itrDetails.setPan(dto.getPan());
            itrDetails.setBirthOrIncorporatedDate(dto.getBirthOrIncorporatedDate());
            itrDetails.setName(dto.getName());
            itrDetails.setFy(dto.getFy());
            itrDetails.setItrFiled(dto.getItrFiled() != null ? dto.getItrFiled() : false);
            itrDetails.setItrType(dto.getItrType());
            itrDetails.setGrossTurnover(dto.getGrossTurnover());
            itrDetails.setGrossTurnoverFormatted(dto.getGrossTurnoverFormatted());
            itrDetails.setExportTurnover(dto.getExportTurnover());
            itrDetails.setExportTurnoverFormatted(dto.getExportTurnoverFormatted());
            itrDetails.setValid(true);
            itrDetails.setPanStatus(dto.getPanStatus());
            itrDetails = itrDetailsRepository.save(itrDetails);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("itrDetailsId", itrDetails.getItrDetailsId());
            dataMap.put("pan", itrDetails.getPan());
            dataMap.put("name", itrDetails.getName());
            response.addData("itrDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "ITR details submitted and saved successfully");
        } catch (Exception e) {
            logger.error("Error submitting ITR details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "ITR submission error: " + e.getMessage());
        }
    }

    public ServiceResponse getItrDetails() {
        ServiceResponse response = new ServiceResponse();
        try {
            Long currentUserId = currentUserService.getCurrentUserId();
            List<CompanyDetails> companies = companyDetailsRepository.findByUserUserId(currentUserId);
            Map<String, Object> dataMap = null;
            if (!companies.isEmpty()) {
                ItrDetails itr = companies.get(0).getItrDetails();
                if (itr != null) {
                    dataMap = new HashMap<>();
                    dataMap.put("pan", itr.getPan());
                    dataMap.put("birthOrIncorporatedDate", itr.getBirthOrIncorporatedDate());
                    dataMap.put("name", itr.getName());
                    dataMap.put("fy", itr.getFy());
                    dataMap.put("itrFiled", itr.getItrFiled());
                    dataMap.put("itrType", itr.getItrType());
                    dataMap.put("grossTurnover", itr.getGrossTurnover());
                    dataMap.put("grossTurnoverFormatted", itr.getGrossTurnoverFormatted());
                    dataMap.put("exportTurnover", itr.getExportTurnover());
                    dataMap.put("exportTurnoverFormatted", itr.getExportTurnoverFormatted());
                    dataMap.put("panStatus", itr.getPanStatus());
                }
            }
            response.addData("itrDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, dataMap == null
                            ? "No ITR details submitted yet for this user."
                            : "ITR details retrieved successfully");
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Failed to retrieve ITR details: " + e.getMessage());
        }
    }

    private JSONObject callAttestrItrApi(String pan, String birthOrIncorporatedDate, String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + authToken);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("pan", pan);
        requestBody.put("birthOrIncorporatedDate", birthOrIncorporatedDate);
        requestBody.put("name", name);
        
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                ITR_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Attestr ITR API returned non-OK status: " + response.getStatusCode());
        }
        
        return new JSONObject(response.getBody());
    }
}
