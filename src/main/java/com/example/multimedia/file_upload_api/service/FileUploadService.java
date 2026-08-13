package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ExtractedDataDTO;
import com.example.multimedia.file_upload_api.dto.FileUploadConfirmationDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

@Service
public class FileUploadService {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private FolderItService folderItService;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private GSTOCRService gstOcrService;

    @Autowired
    private PanOcrService panOcrService;

    @Autowired
    private ChequeOcrService chequeOcrService;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Autowired
    private PanDetailsRepository panDetailsRepository;

    @Autowired
    private ChequeDetailsRepository chequeDetailsRepository;

    @Autowired
    private CertificateOfIncorporationRepository certificateOfIncorporationRepository;

    @Autowired
    private MockResponseService mockResponseService;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    private final Path rootLocation = Paths.get("upload-dir");
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public FileUploadService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    /**
     * Process files and extract data without saving to database
     * 
     * @param gstFile GST file
     * @param panFile PAN file
     * @param chequeFile Cheque file
     * @param coiFile Certificate of Incorporation file (optional)
     * @param superAdminId SuperAdmin ID
     * @return ServiceResponse with extracted data
     */
    public ServiceResponse processAndExtractData(MultipartFile gstFile, MultipartFile panFile, 
            MultipartFile chequeFile, MultipartFile coiFile, Long superAdminId) throws IOException {
        
        ServiceResponse response = new ServiceResponse();

        // Check if superAdmin exists
        SuperAdmin superAdmin = superAdminRepository.findById(superAdminId)
                .orElse(null);
        
        if (superAdmin == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, AppConstants.ERRORCODE, "SuperAdmin not found");
        }

        // Get all companies for the superAdmin
        List<CompanyDetails> existingCompanies = companyDetailsRepository.findByUserUserId(superAdminId);

        // Check for existing filenames
        String gstFileName = gstFile.getOriginalFilename();
        String panFileName = panFile.getOriginalFilename();
        String chequeFileName = chequeFile.getOriginalFilename();
        String coiFileName = coiFile != null ? coiFile.getOriginalFilename() : null;

        for (CompanyDetails company : existingCompanies) {
            if (gstFileName.equals(company.getGstFileName())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "GST file already exists: " + gstFileName);
            }
            if (panFileName.equals(company.getPanFileName())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "PAN file already exists: " + panFileName);
            }
            if (chequeFileName.equals(company.getChequeFileName())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "Cheque file already exists: " + chequeFileName);
            }
            if (coiFileName != null && coiFileName.equals(company.getCoiFileName())) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, AppConstants.ERRORCODE, "COI file already exists: " + coiFileName);
            }
        }

        // Process GST file with real Attestr API only if file doesn't exist
        JSONObject gstOcrResult = gstOcrService.processGSTFile(gstFile);
        logger.info("GST OCR completed with real API");

        // Process other files with mock responses
        JSONObject panOcrResult = mockResponseService.getMockResponse("pan");
        logger.info("PAN OCR completed with mock data");

        JSONObject chequeOcrResult = mockResponseService.getMockResponse("cheque");
        logger.info("Cheque OCR completed with mock data");
        
        // Create formatted response data
        Map<String, Object> formattedData = new HashMap<>();
        
        // Add file names to response
        Map<String, String> fileNames = new HashMap<>();
        fileNames.put("gstFileName", gstFileName);
        fileNames.put("panFileName", panFileName);
        fileNames.put("chequeFileName", chequeFileName);
        if (coiFileName != null) {
            fileNames.put("coiFileName", coiFileName);
        }
        formattedData.put("fileNames", fileNames);
        
        // Format GST Details from real API response
        Map<String, String> gstDetails = new HashMap<>();
        if (gstOcrResult.has("data")) {
            JSONObject gstData = gstOcrResult.getJSONObject("data");
            gstDetails.put("GSTIN Number", gstData.optString("gstin", ""));
            gstDetails.put("Legal Name", gstData.optString("legalName", ""));
            gstDetails.put("Trade Name", gstData.optString("tradeName", ""));
            gstDetails.put("Constitution", gstData.optString("constitution", ""));
            gstDetails.put("Address", gstData.optString("address", ""));
            gstDetails.put("Type", gstData.optString("type", ""));
            gstDetails.put("Registration Date", gstData.optString("registered", ""));
            gstDetails.put("Issue Date", gstData.optString("issued", ""));
            gstDetails.put("Approving Authority", gstData.optString("approvingAuthority", ""));
            gstDetails.put("Jurisdiction", gstData.optString("jurisdiction", ""));
            gstDetails.put("Provisional", String.valueOf(gstData.optBoolean("provisional", false)));
            gstDetails.put("Contact Person", gstData.optString("contactPerson", ""));
            gstDetails.put("Phone Number", gstData.optString("phoneNumber", ""));
        }
        formattedData.put("gst", gstDetails);

        // Format PAN Details
        Map<String, String> panDetails = new HashMap<>();
        if (panOcrResult.has("data")) {
            JSONObject panData = panOcrResult.getJSONObject("data");
            panDetails.put("PAN Number", panData.optString("pan", ""));
            panDetails.put("Name", panData.optString("name", ""));
            panDetails.put("Date of Birth/Incorporation", panData.optString("dateOfBirthIncorporation", ""));
            panDetails.put("Category", panData.optString("category", ""));
            panDetails.put("Address", panData.optString("address", ""));
            panDetails.put("Father's Name", panData.optString("fatherName", ""));
            panDetails.put("Date of Issue", panData.optString("dateOfIssue", ""));
            panDetails.put("Contact Person", panData.optString("contactPerson", ""));
            panDetails.put("Phone Number", panData.optString("phoneNumber", ""));
        }
        formattedData.put("pan", panDetails);

        // Format Cheque Details
        Map<String, String> chequeDetails = new HashMap<>();
        if (chequeOcrResult.has("data")) {
            JSONObject chequeData = chequeOcrResult.getJSONObject("data");
            chequeDetails.put("Account Number", chequeData.optString("Account Number", ""));
            chequeDetails.put("IFSC Code", chequeData.optString("IFSC Code", ""));
            chequeDetails.put("Branch", chequeData.optString("Branch", ""));
            chequeDetails.put("Signatory", chequeData.optString("Signatory", ""));
            chequeDetails.put("Issued Date", chequeData.optString("Issued Date", ""));
            chequeDetails.put("Issued To", chequeData.optString("Issued To", ""));
            chequeDetails.put("Bank Code", chequeData.optString("Bank Code", ""));
            chequeDetails.put("Bank Name", chequeData.optString("Bank Name", ""));
        }
        formattedData.put("cheque", chequeDetails);

        // Add COI number if provided
        if (coiFile != null) {
            Map<String, String> coiData = new HashMap<>();
            coiData.put("cinNumber", "Mock CIN Number");
            formattedData.put("coi", coiData);
        }

        // Prepare success response
        response.addData("extractedData", formattedData);
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(
            response, 
            AppConstants.SUCCESSCODE, 
            "Data extracted successfully"
        );
    }

    /**
     * Converts a PDF file to JPG if the file is a PDF, otherwise returns the original file
     * @param file The file to convert
     * @return A MultipartFile that is either the original file or a converted JPG
     * @throws IOException If there's an error during conversion
     */
    private MultipartFile convertPdfToJpgIfNeeded(MultipartFile file) throws IOException {
        if (file == null) {
            return null;
        }
        
        String contentType = file.getContentType();
        if (contentType != null && contentType.equals("application/pdf")) {
            // Convert PDF to JPG
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            
            // Render the first page
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);
            
            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Create a new MultipartFile from the byte array
            return new MultipartFile() {
                @Override
                public String getName() {
                    return file.getName();
                }
                
                @Override
                public String getOriginalFilename() {
                    String originalName = file.getOriginalFilename();
                    if (originalName != null) {
                        return originalName.substring(0, originalName.lastIndexOf(".")) + ".jpg";
                    }
                    return "converted.jpg";
                }
                
                @Override
                public String getContentType() {
                    return "image/jpeg";
                }
                
                @Override
                public boolean isEmpty() {
                    return imageBytes.length == 0;
                }
                
                @Override
                public long getSize() {
                    return imageBytes.length;
                }
                
                @Override
                public byte[] getBytes() throws IOException {
                    return imageBytes;
                }
                
                @Override
                public java.io.InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(imageBytes);
                }
                
                @Override
                public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
                    java.nio.file.Files.write(dest.toPath(), imageBytes);
                }
            };
        }
        
        // Return the original file if it's not a PDF
        return file;
    }

    public FileUpload uploadFile(MultipartFile file, String documentType, Long userId, String vendorName, String monthYear) throws IOException {
        UserDetail user = userDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        
        Files.copy(file.getInputStream(), this.rootLocation.resolve(newFileName));

        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName(originalFilename);

        try {
            folderItService.uploadFileToFolderIt(file, documentType, vendorName, monthYear);
        } catch (Exception e) {
            logger.error("Failed to upload file to FolderIt", e);
            // Decide if we should throw an exception or just log it
            // For now, let's proceed with saving locally and in the DB
        }
        
        // Ensure fileType is never null
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            // Determine content type based on file extension
            if (fileExtension.equalsIgnoreCase(".pdf")) {
                contentType = "application/pdf";
            } else if (fileExtension.equalsIgnoreCase(".jpg") || fileExtension.equalsIgnoreCase(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileExtension.equalsIgnoreCase(".png")) {
                contentType = "image/png";
            } else {
                contentType = "application/octet-stream"; // Default content type
            }
        }
        fileUpload.setFileType(contentType);
        
        fileUpload.setDocumentType(documentType);
        fileUpload.setFilePath(newFileName);
        fileUpload.setUser(user);

        return fileUploadRepository.save(fileUpload);
    }

    public List<FileUpload> getFilesByUserId(Long userId) {
        return fileUploadRepository.findByUser_UserId(userId);
    }

    public List<FileUpload> getFilesByUserIdAndDocumentType(Long userId, String documentType) {
        return fileUploadRepository.findByUser_UserIdAndDocumentType(userId, documentType);
    }

    public ServiceResponse saveConfirmedData(FileUploadConfirmationDTO confirmationDTO) {
        ServiceResponse response = new ServiceResponse();
        
        // Check if user exists
        UserDetail user = userDetailRepository.findById(confirmationDTO.getUserId())
                .orElse(null);
        
        if (user == null) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response, AppConstants.ERRORCODE, "User not found");
        }
        
        // Check if GST number already exists for this user's super admin
        List<CompanyDetails> existingCompanies = companyDetailsRepository.findByGstinNumberAndSuperAdmin_SuperAdminId(
            confirmationDTO.getGstinNumber(), user.getSuperAdmin().getSuperAdminId());
        boolean gstExistsForUser = false;
        
        if (!existingCompanies.isEmpty()) {
            // Check if any of the companies belong to this user
            for (CompanyDetails company : existingCompanies) {
                if (company.getUser().getUserId().equals(user.getUserId())) {
                    gstExistsForUser = true;
                    break;
                }
            }
            
            if (gstExistsForUser) {
                return serviceControllerUtils.prepareMobileResponseErrorStatus(
                    response, "422", "GST number already exists for this user");
            } else {
                // If it belongs to a different user in the same super admin, we'll allow saving it again
                // This is a business decision - you might want to log this or handle it differently
                // For now, we'll proceed with saving the data
            }
        }
        
        // Create and save CompanyDetails
        CompanyDetails companyDetails = new CompanyDetails();
        companyDetails.setUser(user);
        companyDetails.setGstinNumber(confirmationDTO.getGstinNumber());
        companyDetails.setPanNumber(confirmationDTO.getPanNumber());
        
        // Set default values for required fields to avoid null values
        companyDetails.setLegalTradeName(confirmationDTO.getGstinNumber() + " Company");
        companyDetails.setCompanyName(confirmationDTO.getGstinNumber() + " Company");
        companyDetails.setDateOfRegistration(java.time.LocalDate.now());
        companyDetails.setTypeOfRegistration("Regular");
        companyDetails.setRegisteredAddress("Address to be updated");
        
        // Set company code to avoid validation failure
        String companyCode = confirmationDTO.getGstinNumber();
        if (companyCode == null || companyCode.trim().isEmpty()) {
            companyCode = confirmationDTO.getPanNumber();
        }
        if (companyCode == null || companyCode.trim().isEmpty()) {
            companyCode = "COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        companyDetails.setCompanyCode(companyCode);
        
        companyDetails = companyDetailsRepository.save(companyDetails);
        
        // Create and save PanDetails
        PanDetails panDetails = new PanDetails();
        panDetails.setCompany(companyDetails);
        panDetails.setPanNumber(confirmationDTO.getPanNumber());
        
        // Set default values for required fields to avoid null values
        panDetails.setName(confirmationDTO.getPanNumber() + " Entity");
        panDetails.setDateOfBirthIncorporation(java.time.LocalDate.now().toString());
        panDetails.setCategory("Company");
        
        panDetails = panDetailsRepository.save(panDetails);
        
        // Create and save ChequeDetails
        ChequeDetails chequeDetails = new ChequeDetails();
        chequeDetails.setCompany(companyDetails);
        chequeDetails.setAccountNumber(confirmationDTO.getAccountNumber());
        
        // Set default values for required fields to avoid null values
        chequeDetails.setBank("Bank to be updated");
        chequeDetails.setBranch("Branch to be updated");
        chequeDetails.setIfsc("IFSC to be updated");
        
        chequeDetails = chequeDetailsRepository.save(chequeDetails);
        
        // Create and save CertificateOfIncorporation if CIN number is provided
        CertificateOfIncorporation coi = null;
        if (confirmationDTO.getCinNo() != null && !confirmationDTO.getCinNo().isEmpty()) {
            try {
                // Check if CIN number already exists
                coi = certificateOfIncorporationRepository.findByCinNumber(confirmationDTO.getCinNo());
                
                if (coi == null) {
                    // Create new COI if it doesn't exist
                    coi = new CertificateOfIncorporation();
                    coi.setCinNumber(confirmationDTO.getCinNo());
                    coi.setCompany(companyDetails);
                    coi = certificateOfIncorporationRepository.save(coi);
        } else {
                    // If COI exists, update its company details
                    coi.setCompany(companyDetails);
                    coi = certificateOfIncorporationRepository.save(coi);
                }
                
                // Update CompanyDetails with COI reference
                companyDetails.setCertificateOfIncorporation(coi);
                companyDetailsRepository.save(companyDetails);
            } catch (Exception e) {
                // Log the error but continue with the process
                System.err.println("Error handling CIN number: " + e.getMessage());
                e.printStackTrace(); // Add stack trace for better debugging
            }
        }
        
        // Prepare success response with simplified data to avoid circular references
        Map<String, Object> savedData = new HashMap<>();
        
        // Add company details
        Map<String, Object> companyData = new HashMap<>();
        companyData.put("companyId", companyDetails.getCompanyId());
        companyData.put("gstinNumber", companyDetails.getGstinNumber());
        companyData.put("legalTradeName", companyDetails.getLegalTradeName());
        companyData.put("dateOfRegistration", companyDetails.getDateOfRegistration());
        companyData.put("panTinCst", companyDetails.getPanTinCst());
        companyData.put("typeOfRegistration", companyDetails.getTypeOfRegistration());
        companyData.put("registeredAddress", companyDetails.getRegisteredAddress());
        companyData.put("companyName", companyDetails.getCompanyName());
        companyData.put("panNumber", companyDetails.getPanNumber());
        savedData.put("companyDetails", companyData);
        
        // Add PAN details
        Map<String, Object> panData = new HashMap<>();
        panData.put("panDetailsId", panDetails.getPanDetailsId());
        panData.put("panNumber", panDetails.getPanNumber());
        panData.put("name", panDetails.getName());
        panData.put("dateOfBirthIncorporation", panDetails.getDateOfBirthIncorporation());
        panData.put("fathersName", panDetails.getFathersName());
        panData.put("category", panDetails.getCategory());
        savedData.put("panDetails", panData);
        
        // Add cheque details
        Map<String, Object> chequeData = new HashMap<>();
        chequeData.put("chequeDetailsId", chequeDetails.getChequeDetailsId());
        chequeData.put("accountNumber", chequeDetails.getAccountNumber());
        chequeData.put("bank", chequeDetails.getBank());
        chequeData.put("branch", chequeDetails.getBranch());
        chequeData.put("ifsc", chequeDetails.getIfsc());
        chequeData.put("code", chequeDetails.getCode());
        chequeData.put("issuedTo", chequeDetails.getIssuedTo());
        chequeData.put("signatory", chequeDetails.getSignatory());
        chequeData.put("issued", chequeDetails.getIssued());
        savedData.put("chequeDetails", chequeData);
        
        // Add COI details if available
        if (coi != null) {
            Map<String, Object> coiData = new HashMap<>();
            coiData.put("cinNo", coi.getCinNumber());
            savedData.put("certificateOfIncorporation", coiData);
        }
        
        response.addData("savedData", savedData);
        
        return serviceControllerUtils.prepareMobileResponseSuccessStatus(
            response, AppConstants.SUCCESSCODE, "Data saved successfully");
    }
}
