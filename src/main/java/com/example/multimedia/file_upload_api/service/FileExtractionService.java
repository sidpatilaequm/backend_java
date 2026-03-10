package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import com.example.multimedia.file_upload_api.entity.CompanyDetails;
import com.example.multimedia.file_upload_api.repository.CompanyDetailsRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileExtractionService {

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Value("${use.mock.responses}")
    private boolean useMockResponses;

    public ServiceResponse extractDataFromFiles(MultipartFile gstFile, MultipartFile panFile, 
                                              MultipartFile chequeFile, MultipartFile coiFile,
                                              String authKey) {
        ServiceResponse response = new ServiceResponse();

        try {
            // Get current super admin from security context
            SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();

            // Get authorization for the authKey
            Authorization authorization = authorizationRepository.findByAuthKeyIgnoreCase(authKey)
                    .orElseThrow(() -> new RuntimeException("Invalid authKey. Must be one of: vendor, customer, super_admin"));

            // Get all companies for the current super admin
            List<CompanyDetails> existingCompanies = companyDetailsRepository.findBySuperAdminSuperAdminId(currentSuperAdmin.getSuperAdminId());

            // Check for existing filenames with same authKey
            String gstFileName = gstFile.getOriginalFilename();
            String panFileName = panFile.getOriginalFilename();
            String chequeFileName = chequeFile.getOriginalFilename();
            String coiFileName = coiFile != null ? coiFile.getOriginalFilename() : null;

            for (CompanyDetails company : existingCompanies) {
                // Only check for file existence if the company has the same auth_key
                if (company.getAuthKey().equalsIgnoreCase(authKey)) {
                    if (gstFileName != null && gstFileName.equals(company.getGstFileName())) {
                        return serviceControllerUtils.prepareMobileResponseErrorStatus(
                            response, AppConstants.ERRORCODE, "GST file already exists for a " + authKey + ": " + gstFileName);
                    }
                    if (panFileName != null && panFileName.equals(company.getPanFileName())) {
                        return serviceControllerUtils.prepareMobileResponseErrorStatus(
                            response, AppConstants.ERRORCODE, "PAN file already exists for a " + authKey + ": " + panFileName);
                    }
                    if (chequeFileName != null && chequeFileName.equals(company.getChequeFileName())) {
                        return serviceControllerUtils.prepareMobileResponseErrorStatus(
                            response, AppConstants.ERRORCODE, "Cheque file already exists for a " + authKey + ": " + chequeFileName);
                    }
                    if (coiFileName != null && coiFileName.equals(company.getCoiFileName())) {
                        return serviceControllerUtils.prepareMobileResponseErrorStatus(
                            response, AppConstants.ERRORCODE, "COI file already exists for a " + authKey + ": " + coiFileName);
                    }
                }
            }

            if (useMockResponses) {
                Map<String, Object> extractedData = new HashMap<>();
                
                // GST Data
                Map<String, String> gstData = new HashMap<>();
                gstData.put("Registration Date", "05/10/2024");
                gstData.put("PAN/TIN/CST", "ABCDP3063A");
                gstData.put("Registered Address", "10th Floor, 1006, Road, Ahmedabad, B Square-1, Gujarat, Near Neptune House, 380058 Ambli Brts,");
                gstData.put("Legal Trade Name", "ACME INFO PRIVATE LIMITED");
                gstData.put("GSTIN Number", "24ABCD1234R1ZN");
                gstData.put("Registration Type", "Regular");
                extractedData.put("gst", gstData);

                // Cheque Data
                Map<String, String> chequeData = new HashMap<>();
                chequeData.put("Account Number", "12345678901234");
                chequeData.put("IFSC Code", "HDFC0001234");
                chequeData.put("Branch", "Mumbai Main");
                chequeData.put("Signatory", "John Doe");
                chequeData.put("Issued Date", "08/04/2024");
                chequeData.put("Issued To", "ACME INFO PRIVATE LIMITED");
                chequeData.put("Bank Code", "CHQ123456");
                chequeData.put("Bank Name", "HDFC Bank");
                extractedData.put("cheque", chequeData);

                // PAN Data
                Map<String, String> panData = new HashMap<>();
                panData.put("Category", "BUSINESS");
                panData.put("Date of Birth/Incorporation", "13/07/2017");
                panData.put("PAN Number", "ABCDP3063A");
                panData.put("Father's Name", "Not Available");
                panData.put("Name", "PEGADROID IQ SOLUTIONS PRIVATE LIMITED");
                extractedData.put("pan", panData);

                // COI Data
                Map<String, String> coiData = new HashMap<>();
                coiData.put("cinNumber", "U72200GJ2017PTC123456");
                coiData.put("Company Name", "ACME INFO PRIVATE LIMITED");
                coiData.put("Date of Incorporation", "13/07/2017");
                coiData.put("Registered Office", "10th Floor, 1006, Road, Ahmedabad, B Square-1, Gujarat, Near Neptune House, 380058 Ambli Brts,");
                coiData.put("Authorized Capital", "1000000");
                coiData.put("Paid-up Capital", "1000000");
                coiData.put("Company Category", "Company limited by Shares");
                coiData.put("Company Sub Category", "Non-govt company");
                coiData.put("Class of Company", "Private");
                coiData.put("Date of Last AGM", "30/09/2023");
                coiData.put("Date of Balance Sheet", "31/03/2023");
                extractedData.put("coi", coiData);

                // Add file names
                Map<String, String> fileNames = new HashMap<>();
                fileNames.put("gstFileName", gstFile.getOriginalFilename());
                fileNames.put("panFileName", panFile.getOriginalFilename());
                fileNames.put("chequeFileName", chequeFile.getOriginalFilename());
                if (coiFile != null) {
                    fileNames.put("coiFileName", coiFile.getOriginalFilename());
                }
                extractedData.put("fileNames", fileNames);

                response.addData("extractedData", extractedData);
                return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                    response,
                    AppConstants.SUCCESSCODE,
                    "Data extracted successfully"
                );
            }

            // Real implementation would go here
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Real implementation not available"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to extract data: " + e.getMessage()
            );
        }
    }
} 