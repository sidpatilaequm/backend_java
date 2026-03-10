package com.example.multimedia.file_upload_api.dto;

import lombok.Data;

@Data
public class AuthorizationDTO {

    private int authId;
    private String authName;
    private String authKey;
    private boolean active;

}
