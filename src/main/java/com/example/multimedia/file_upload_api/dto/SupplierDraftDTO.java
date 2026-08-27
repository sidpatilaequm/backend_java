package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

/** Payload for POST .../draft — matches SupplierRegistration's own field shape 1:1. */
@Data
public class SupplierDraftDTO {
    private Long registrationId;
    private String resumeCode;
    /** True only for a deliberate "Save draft" click on the frontend — never the silent
     *  background autosave — so the draft-saved email fires every explicit save. */
    private Boolean explicitSave;

    private String vendorName;
    private String address;

    private String contact1Name;
    private String contact1Role;
    private String contact1Email;
    private String contact1Phone;

    private String contact2Name;
    private String contact2Role;
    private String contact2Email;
    private String contact2Phone;

    private Integer primaryContact;

    /** Comma-separated on the wire, same as how it's stored. */
    private String businessTypes;
    private String businessScope;
    private String companyType;
    private String telephone;
    private String fax;
    private String weeklyOff;
    private String annualTurnover;
    private String turnoverYear;
    private String regulatoryActs;
    private String manpowerOffice;
    private String manpowerSupervisor;
    private String manpowerWorkmen;
    private String shiftsPerDay;
    private String spareCapacity;
    private String floorSpace;
    private String equipmentFacilities;
    /** JSON array on the wire, stored as-is. */
    private String directorsJson;
    private String machineryJson;
    /** {questionId, textValue?, optionIds?}[] — validated/persisted into Form Studio's tables at submit. */
    private String dynamicAnswersJson;
    /** Which questionnaire dynamicAnswersJson was answered against — see the entity field's doc. */
    private Integer dynamicQuestionnaireProcessId;
    private Boolean declarationAccepted;

    private String gstNumber;
    private String panNumber;
    private String msmeNumber;
    private String cinNumber;

    private String beneficiaryName;
    private String accountNumber;
    private String ifscCode;
    private String bankName;

    private String isoCertificateNo;
    private String isoCertifyingBody;
    private String isoExpiry;

    private String as9100dCertificateNo;
    private String as9100dCertifyingBody;
    private String as9100dExpiry;

    private String nadcapCertificateNo;
    private String nadcapExpiry;
}
