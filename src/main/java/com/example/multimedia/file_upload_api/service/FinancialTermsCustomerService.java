package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.FinancialTermsCustomerDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.entity.FinancialTermsCustomer;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.FinancialTermsCustomerRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class FinancialTermsCustomerService {
    private static final Logger logger = LoggerFactory.getLogger(FinancialTermsCustomerService.class);

    @Autowired
    private FinancialTermsCustomerRepository financialTermsCustomerRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private CurrentUserService currentUserService;

    @Transactional
    public ServiceResponse saveFinancialTermsCustomer(FinancialTermsCustomerDTO dto) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Validate GST number and auth key
            if (!"customer".equalsIgnoreCase(dto.getAuthKey())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Financial terms can only be added for customers"
                );
            }

            // Find company by GST number for current super admin
            List<CompanyDetails> companies = companyDetailsRepository.findByGstinNumberAndSuperAdmin_SuperAdminIdAndAuthKey(
                dto.getGstinNumber(), superAdmin.getSuperAdminId(), "customer");
            if (companies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Company not found with GST number: " + dto.getGstinNumber() + " in your account"
                );
            }

            CompanyDetails company = companies.get(0);

            // Check if financial terms already exist for this company
            List<FinancialTermsCustomer> existingTerms = financialTermsCustomerRepository.findByCompany_CompanyId(company.getCompanyId());
            if (!existingTerms.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Financial terms already exist for GST number: " + dto.getGstinNumber()
                );
            }

            // Create and save financial terms
            FinancialTermsCustomer financialTerms = new FinancialTermsCustomer();
            financialTerms.setCompany(company);
            financialTerms.setDeliveryTerms(dto.getDeliveryTerms());
            financialTerms.setDeliveryLocation(dto.getDeliveryLocation());
            financialTerms.setBlockIndicator(dto.getBlockIndicator());
            financialTerms.setOrderCurrency(dto.getOrderCurrency());
            financialTerms.setDeliveryDays(dto.getDeliveryDays());
            financialTerms.setReconciliationAccount(dto.getReconciliationAccount());
            financialTerms.setTermsOfPayment(dto.getTermsOfPayment());
            financialTerms.setIsActive(true);

            financialTerms = financialTermsCustomerRepository.save(financialTerms);

            // Prepare success response
            response.addData("financialTerms", financialTerms);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Financial terms saved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to save financial terms: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public ServiceResponse getFinancialTermsCustomerByGstAndAuthKey(String gstinNumber, String authKey) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Validate auth key
            if (!"customer".equalsIgnoreCase(authKey)) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Financial terms can only be retrieved for customers"
                );
            }

            // Find company by GST number for current super admin
            List<CompanyDetails> companies = companyDetailsRepository.findByGstinNumberAndSuperAdmin_SuperAdminIdAndAuthKey(
                gstinNumber, superAdmin.getSuperAdminId(), "customer");
            if (companies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Company not found with GST number: " + gstinNumber + " in your account"
                );
            }

            CompanyDetails company = companies.get(0);

            // Get financial terms for the company
            List<FinancialTermsCustomer> financialTerms = financialTermsCustomerRepository.findByCompany_CompanyId(company.getCompanyId());

            if (financialTerms.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "No financial terms found for GST number: " + gstinNumber
                );
            }

            // Prepare company details
            Map<String, Object> companyDetails = new HashMap<>();
            companyDetails.put("gstinNumber", company.getGstinNumber());
            companyDetails.put("panNumber", company.getPanNumber());
            companyDetails.put("legalTradeName", company.getLegalTradeName());
            companyDetails.put("companyName", company.getCompanyName());

            // Add bank details if available
            if (company.getChequeDetails() != null) {
                Map<String, String> bankDetails = new HashMap<>();
                bankDetails.put("accountNumber", company.getChequeDetails().getAccountNumber());
                bankDetails.put("bank", company.getChequeDetails().getBank());
                bankDetails.put("branch", company.getChequeDetails().getBranch());
                bankDetails.put("ifsc", company.getChequeDetails().getIfsc());
                companyDetails.put("bankDetails", bankDetails);
            }

            // Prepare success response
            response.addData("companyDetails", companyDetails);
            response.addData("financialTerms", financialTerms);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Financial terms and company details retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve financial terms: " + e.getMessage()
            );
        }
    }

    @Transactional
    public ServiceResponse updateFinancialTermsCustomer(FinancialTermsCustomerDTO dto) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin
            SuperAdmin superAdmin = currentUserService.getCurrentSuperAdmin();
            
            // Validate GST number and auth key
            if (!"customer".equalsIgnoreCase(dto.getAuthKey())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Financial terms can only be updated for customers"
                );
            }

            // Find company by GST number for current super admin
            List<CompanyDetails> companies = companyDetailsRepository.findByGstinNumberAndSuperAdmin_SuperAdminIdAndAuthKey(
                dto.getGstinNumber(), superAdmin.getSuperAdminId(), "customer");
            if (companies.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "Company not found with GST number: " + dto.getGstinNumber() + " in your account"
                );
            }

            CompanyDetails company = companies.get(0);

            // Get existing financial terms for this company
            List<FinancialTermsCustomer> existingTerms = financialTermsCustomerRepository.findByCompany_CompanyId(company.getCompanyId());
            if (existingTerms.isEmpty()) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response,
                    AppConstants.ERRORCODE,
                    "No financial terms found to update for GST number: " + dto.getGstinNumber()
                );
            }

            // Update the first (and should be only) financial terms record
            FinancialTermsCustomer financialTerms = existingTerms.get(0);
            financialTerms.setDeliveryTerms(dto.getDeliveryTerms());
            financialTerms.setDeliveryLocation(dto.getDeliveryLocation());
            financialTerms.setBlockIndicator(dto.getBlockIndicator());
            financialTerms.setOrderCurrency(dto.getOrderCurrency());
            financialTerms.setDeliveryDays(dto.getDeliveryDays());
            financialTerms.setReconciliationAccount(dto.getReconciliationAccount());
            financialTerms.setTermsOfPayment(dto.getTermsOfPayment());
            financialTerms.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

            financialTerms = financialTermsCustomerRepository.save(financialTerms);

            // Prepare success response
            response.addData("financialTerms", financialTerms);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Financial terms updated successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to update financial terms: " + e.getMessage()
            );
        }
    }
} 