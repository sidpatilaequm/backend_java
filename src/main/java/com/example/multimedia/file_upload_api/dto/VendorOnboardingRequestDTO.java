package com.example.multimedia.file_upload_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorOnboardingRequestDTO {
    
    @NotBlank(message = "Vendor Legal Entity Name is required")
    private String vendorLegalEntityName;

    @NotBlank(message = "Vendor Address is required")
    private String vendorAddress;

    @NotBlank(message = "Vendor Contact Name is required")
    private String vendorContactName;

    @NotBlank(message = "Vendor Designation is required")
    private String vendorDesignation;

    @NotBlank(message = "Vendor Email is required")
    private String vendorEmail;

    @NotBlank(message = "Vendor Phone Number is required")
    private String vendorPhoneNumber;

    @NotNull(message = "Admin ID is required")
    private Long adminId;
}
