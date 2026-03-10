package com.example.multimedia.file_upload_api.dto;

import com.example.multimedia.file_upload_api.entity.CertificateOfIncorporation;
import com.example.multimedia.file_upload_api.entity.ChequeDetails;
import com.example.multimedia.file_upload_api.entity.PanDetails;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDetailsDTO {
    private String companyName;
    private String gstinNumber;
    private String legalTradeName;
    private String registeredAddress;
    private String panNumber;
    private String panTinCst;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateOfRegistration;
    
    private String typeOfRegistration;
    private String authKey;
    private String gstFileName;
    private String panFileName;
    private String chequeFileName;
    private String coiFileName;
}
