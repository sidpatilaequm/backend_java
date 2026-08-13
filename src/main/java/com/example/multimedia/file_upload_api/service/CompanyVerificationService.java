package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.CompanySubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CertificateOfIncorporation;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.CertificateOfIncorporationRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CompanyVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyVerificationService.class);
    private static final String COI_URL = "https://api.attestr.com/api/v2/public/corpx/business/master";

    @Autowired
    private CertificateOfIncorporationRepository coiRepository;

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

    public ServiceResponse verifyCompanyRegistration(String reg, Long companyId) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Always resolve company by userId from token
            Long currentUserId = currentUserService.getCurrentUserId();
            List<CompanyDetails> userCompanies = companyDetailsRepository.findByUserUserId(currentUserId);
            CompanyDetails company = userCompanies.isEmpty() ? null : userCompanies.get(0);

            String finalReg = (reg != null && !reg.trim().isEmpty()) ? reg : (company != null ? company.getGstinNumber() : null);
            if (finalReg == null && company != null) {
                CertificateOfIncorporation existingCoi = company.getCertificateOfIncorporation();
                if (existingCoi != null) {
                    finalReg = existingCoi.getCinNumber();
                }
            }

            if (finalReg == null || finalReg.trim().isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "CIN / LLPIN / FCRN registration number is required.");
            }

            String prefix = finalReg.substring(0, 1).toUpperCase();
            if (finalReg.startsWith("U")) {
                logger.info("Verifying Registration Number: {} (Detected: CIN)", finalReg);
            } else if (finalReg.startsWith("L")) {
                logger.info("Verifying Registration Number: {} (Detected: LLPIN)", finalReg);
            } else if (finalReg.startsWith("F")) {
                logger.info("Verifying Registration Number: {} (Detected: FCRN)", finalReg);
            }

            JSONObject coiResult;
            if (mockResponseService.isUseMockResponses()) {
                logger.info("Using mock response for Company Verification");
                coiResult = mockResponseService.getMockResponse("coi");
            } else {
                logger.info("Calling Attestr Company Verification API for registration number: {}", finalReg);
                coiResult = callAttestrCompanyApi(finalReg);
            }

            if (!coiResult.optBoolean("valid", false)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "Company registration verification failed or registration number is invalid.");
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("cinNumber", finalReg);
            dataMap.put("businessName", coiResult.optString("businessName", coiResult.optString("Company Name")));
            dataMap.put("rocCode", coiResult.optString("rocCode"));
            dataMap.put("registrationNumber", coiResult.optString("registrationNumber"));
            dataMap.put("category", coiResult.optString("category", coiResult.optString("Company Category")));
            dataMap.put("subCategory", coiResult.optString("subCategory", coiResult.optString("Company Sub Category")));
            dataMap.put("companyClass", coiResult.optString("class", coiResult.optString("Class of Company")));
            dataMap.put("authorizedCapital", coiResult.optString("authorizedCapital", coiResult.optString("Authorized Capital")));
            dataMap.put("paidCapital", coiResult.optString("paidCapital", coiResult.optString("Paid-up Capital")));
            dataMap.put("incorporatedDate", coiResult.optString("incorporatedDate", coiResult.optString("Date of Incorporation")));
            dataMap.put("email", coiResult.optString("email"));
            dataMap.put("listed", coiResult.optBoolean("listed", false));
            dataMap.put("lastAGMDate", coiResult.optString("lastAGMDate", coiResult.optString("Date of Last AGM")));
            dataMap.put("lastBSDate", coiResult.optString("lastBSDate", coiResult.optString("Date of Balance Sheet")));
            dataMap.put("active", coiResult.optBoolean("active", true));
            dataMap.put("status", coiResult.optString("status"));
            
            dataMap.put("addressesJson", coiResult.optJSONArray("addresses") != null ? 
                    coiResult.optJSONArray("addresses").toString() : null);
            dataMap.put("directorsJson", coiResult.optJSONArray("directorsAndSignatories") != null ? 
                    coiResult.optJSONArray("directorsAndSignatories").toString() : null);
            dataMap.put("chargesJson", coiResult.optJSONArray("charges") != null ? 
                    coiResult.optJSONArray("charges").toString() : null);
            dataMap.put("efilingsJson", coiResult.optJSONArray("efilings") != null ? 
                    coiResult.optJSONArray("efilings").toString() : null);

            response.addData("companyRegistration", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "Company registration details verified successfully (unsubmitted)");

        } catch (Exception e) {
            logger.error("Error verifying Company registration details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Company Verification error: " + e.getMessage());
        }
    }

    public ServiceResponse submitCompany(CompanySubmitDTO dto) {
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

            CertificateOfIncorporation coi = coiRepository.findByCompanyCompanyId(company.getCompanyId());
            if (coi == null) {
                coi = new CertificateOfIncorporation();
                coi.setCompany(company);
            }
            coi.setCinNumber(dto.getReg());
            coi.setBusinessName(dto.getBusinessName());
            coi.setRocCode(dto.getRocCode());
            coi.setRegistrationNumber(dto.getRegistrationNumber());
            coi.setCategory(dto.getCategory());
            coi.setSubCategory(dto.getSubCategory());
            coi.setCompanyClass(dto.getCompanyClass());
            coi.setAuthorizedCapital(dto.getAuthorizedCapital());
            coi.setPaidCapital(dto.getPaidCapital());
            coi.setIncorporatedDate(dto.getIncorporatedDate());
            coi.setEmail(dto.getEmail());
            coi.setListed(dto.getListed() != null ? dto.getListed() : false);
            coi.setLastAGMDate(dto.getLastAGMDate());
            coi.setLastBSDate(dto.getLastBSDate());
            coi.setActive(dto.getActive() != null ? dto.getActive() : true);
            coi.setStatus(dto.getStatus());
            coi.setAddressesJson(dto.getAddressesJson());
            coi.setDirectorsJson(dto.getDirectorsJson());
            coi.setChargesJson(dto.getChargesJson());
            coi.setEfilingsJson(dto.getEfilingsJson());
            coi = coiRepository.save(coi);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("certificateOfIncorporationId", coi.getCertificateOfIncorporationId());
            dataMap.put("cinNumber", coi.getCinNumber());
            dataMap.put("businessName", coi.getBusinessName());
            response.addData("companyRegistration", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "Company registration details submitted and saved successfully");
        } catch (Exception e) {
            logger.error("Error submitting Company registration details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Company submission error: " + e.getMessage());
        }
    }

    public ServiceResponse getCompanyRegistrationDetails() {
        ServiceResponse response = new ServiceResponse();
        try {
            Long currentUserId = currentUserService.getCurrentUserId();
            List<CompanyDetails> companies = companyDetailsRepository.findByUserUserId(currentUserId);
            Map<String, Object> dataMap = null;
            if (!companies.isEmpty()) {
                CertificateOfIncorporation coi = companies.get(0).getCertificateOfIncorporation();
                if (coi != null) {
                    dataMap = new HashMap<>();
                    dataMap.put("cinNumber", coi.getCinNumber());
                    dataMap.put("businessName", coi.getBusinessName());
                    dataMap.put("rocCode", coi.getRocCode());
                    dataMap.put("registrationNumber", coi.getRegistrationNumber());
                    dataMap.put("category", coi.getCategory());
                    dataMap.put("subCategory", coi.getSubCategory());
                    dataMap.put("companyClass", coi.getCompanyClass());
                    dataMap.put("authorizedCapital", coi.getAuthorizedCapital());
                    dataMap.put("paidCapital", coi.getPaidCapital());
                    dataMap.put("incorporatedDate", coi.getIncorporatedDate());
                    dataMap.put("email", coi.getEmail());
                    dataMap.put("listed", coi.getListed());
                    dataMap.put("lastAGMDate", coi.getLastAGMDate());
                    dataMap.put("lastBSDate", coi.getLastBSDate());
                    dataMap.put("active", coi.getActive());
                    dataMap.put("status", coi.getStatus());
                    dataMap.put("addressesJson", coi.getAddressesJson());
                    dataMap.put("directorsJson", coi.getDirectorsJson());
                    dataMap.put("chargesJson", coi.getChargesJson());
                    dataMap.put("efilingsJson", coi.getEfilingsJson());
                }
            }
            response.addData("companyRegistration", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, dataMap == null
                            ? "No COI details submitted yet for this user."
                            : "Company registration details retrieved successfully");
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Failed to retrieve Company registration details: " + e.getMessage());
        }
    }

    private JSONObject callAttestrCompanyApi(String reg) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + authToken);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("reg", reg);
        
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                COI_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Attestr Company Verification API returned non-OK status: " + response.getStatusCode());
        }
        
        return new JSONObject(response.getBody());
    }
}
