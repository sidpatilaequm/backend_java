package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ChequeSubmitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.ChequeDetails;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.ChequeDetailsRepository;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
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
public class ChequeVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(ChequeVerificationService.class);

    @Autowired
    private ChequeOcrService chequeOcrService;

    @Autowired
    private ChequeDetailsRepository chequeDetailsRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    public ServiceResponse verifyCheque(MultipartFile file, String accountNumber, String ifscCode, Long companyId) {
        ServiceResponse response = new ServiceResponse();
        try {
            JSONObject chequeResult;
            if (file != null && !file.isEmpty()) {
                chequeResult = chequeOcrService.processChequeFile(file);
            } else if (accountNumber != null && !accountNumber.trim().isEmpty() && ifscCode != null && !ifscCode.trim().isEmpty()) {
                chequeResult = getMockChequeResponse(accountNumber, ifscCode);
            } else {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                        response, AppConstants.ERRORCODE, "Either Cheque file or Account Number and IFSC Code must be provided.");
            }

            JSONObject chequeData = chequeResult.optJSONObject("data");
            if (chequeData == null) {
                if (chequeResult.has("Account Number")) {
                    chequeData = chequeResult;
                } else {
                    return serviceControllerUtils.prepareMobileResponseErrorStatus(
                            response, AppConstants.ERRORCODE, "Failed to parse cheque verification details.");
                }
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("accountNumber", chequeData.optString("accountNumber", chequeData.optString("Account Number", accountNumber)));
            dataMap.put("ifscCode", chequeData.optString("ifscCode", chequeData.optString("IFSC Code", ifscCode)));
            dataMap.put("branch", chequeData.optString("branch", chequeData.optString("Branch", "Main Branch")));
            dataMap.put("bankName", chequeData.optString("bankName", chequeData.optString("Bank Name", "Mock Bank")));
            dataMap.put("signatory", chequeData.optString("signatory", chequeData.optString("Signatory", "Not Available")));
            dataMap.put("issuedTo", chequeData.optString("issuedTo", chequeData.optString("Issued To", "Not Available")));
            dataMap.put("issued", chequeData.optString("issued", chequeData.optString("Issued Date", "Not Available")));
            dataMap.put("code", chequeData.optString("code", chequeData.optString("Bank Code", "Not Available")));

            response.addData("chequeDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "Cheque details verified successfully (unsubmitted)");

        } catch (Exception e) {
            logger.error("Error verifying Cheque Details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Cheque Verification error: " + e.getMessage());
        }
    }

    public ServiceResponse submitCheque(ChequeSubmitDTO dto) {
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

            ChequeDetails chequeDetails = chequeDetailsRepository.findByCompanyCompanyId(company.getCompanyId())
                    .orElse(new ChequeDetails());
            chequeDetails.setCompany(company);
            chequeDetails.setAccountNumber(dto.getAccountNumber());
            chequeDetails.setIfsc(dto.getIfscCode());
            chequeDetails.setBranch(dto.getBranch());
            chequeDetails.setBank(dto.getBankName());
            chequeDetails.setSignatory(dto.getSignatory());
            chequeDetails.setIssuedTo(dto.getIssuedTo());
            chequeDetails.setIssued(dto.getIssued());
            chequeDetails.setCode(dto.getCode());
            chequeDetails = chequeDetailsRepository.save(chequeDetails);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("chequeDetailsId", chequeDetails.getChequeDetailsId());
            dataMap.put("accountNumber", chequeDetails.getAccountNumber());
            dataMap.put("ifscCode", chequeDetails.getIfsc());
            response.addData("chequeDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, "Cheque details submitted and saved successfully");
        } catch (Exception e) {
            logger.error("Error submitting Cheque Details", e);
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Cheque submission error: " + e.getMessage());
        }
    }

    public ServiceResponse getChequeDetails() {
        ServiceResponse response = new ServiceResponse();
        try {
            Long currentUserId = currentUserService.getCurrentUserId();
            List<CompanyDetails> companies = companyDetailsRepository.findByUserUserId(currentUserId);
            Map<String, Object> dataMap = null;
            if (!companies.isEmpty()) {
                ChequeDetails cheque = companies.get(0).getChequeDetails();
                if (cheque != null) {
                    dataMap = new HashMap<>();
                    dataMap.put("accountNumber", cheque.getAccountNumber());
                    dataMap.put("ifscCode", cheque.getIfsc());
                    dataMap.put("bankName", cheque.getBank());
                    dataMap.put("branch", cheque.getBranch());
                    dataMap.put("signatory", cheque.getSignatory());
                    dataMap.put("issuedTo", cheque.getIssuedTo());
                    dataMap.put("issued", cheque.getIssued());
                    dataMap.put("code", cheque.getCode());
                }
            }
            response.addData("chequeDetails", dataMap);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response, AppConstants.SUCCESSCODE, dataMap == null
                            ? "No Cheque details submitted yet for this user."
                            : "Cheque details retrieved successfully");
        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Failed to retrieve Cheque details: " + e.getMessage());
        }
    }

    private JSONObject getMockChequeResponse(String accountNumber, String ifscCode) {
        JSONObject res = new JSONObject();
        res.put("status", "success");
        JSONObject data = new JSONObject();
        data.put("accountNumber", accountNumber);
        data.put("ifscCode", ifscCode);
        data.put("branch", "Mumbai Fort");
        data.put("bankName", "HDFC Bank");
        data.put("signatory", "John Doe");
        data.put("issuedTo", "ACME INFO PRIVATE LIMITED");
        data.put("issued", "08/04/2024");
        data.put("code", "CHQ123456");
        res.put("data", data);
        return res;
    }
}
