package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.AuthorizationDTO;

import java.util.List;

public interface AuthorizationService {

    AuthorizationDTO createAuthorization(AuthorizationDTO dto);
    AuthorizationDTO updateAuthorization(int authId, AuthorizationDTO dto);
    List<AuthorizationDTO> getAllAuthorizations();
    AuthorizationDTO getAuthorizationById(int authId);
}
