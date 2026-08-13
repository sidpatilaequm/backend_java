package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class ItrSubmitDTO {
    private String pan;
    private String birthOrIncorporatedDate;
    private String name;
    private String fy;
    private Boolean itrFiled;
    private String itrType;
    private String grossTurnover;
    private String grossTurnoverFormatted;
    private String exportTurnover;
    private String exportTurnoverFormatted;
    private String panStatus;
    private Long companyId; // Optional admin override
}
