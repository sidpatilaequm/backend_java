package com.example.multimedia.file_upload_api.service;


import com.example.multimedia.file_upload_api.dto.AuthorizationDTO;
import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private AuthorizationRepository repository;

    private AuthorizationDTO convertToDTO(Authorization auth) {
        AuthorizationDTO dto = new AuthorizationDTO();
        dto.setAuthName(auth.getAuthName());
        dto.setAuthKey(auth.getAuthKey());
        dto.setActive(auth.isActive());
        return dto;
    }

    private Authorization convertToEntity(AuthorizationDTO dto) {
        Authorization auth = new Authorization();
//        auth.setAuthId(dto.getAuthId());
        auth.setAuthName(dto.getAuthName());
        auth.setAuthKey(dto.getAuthKey());
        auth.setActive(dto.isActive());
        return auth;
    }

    @Override
    public AuthorizationDTO createAuthorization(AuthorizationDTO dto) {
        Authorization auth = convertToEntity(dto);
        return convertToDTO(repository.save(auth));
    }

    @Override
    public AuthorizationDTO updateAuthorization(int authId, AuthorizationDTO dto) {
        Optional<Authorization> optional = repository.findById(authId);
        if (optional.isPresent()) {
            Authorization auth = optional.get();
            auth.setAuthName(dto.getAuthName());
            auth.setAuthKey(dto.getAuthKey());
            auth.setActive(dto.isActive());
            return convertToDTO(repository.save(auth));
        } else {
            throw new RuntimeException("Authorization not found with ID: " + authId);
        }
    }

    @Override
    public List<AuthorizationDTO> getAllAuthorizations() {
        return repository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AuthorizationDTO getAuthorizationById(int authId) {
        Authorization auth = repository.findById(authId)
                .orElseThrow(() -> new RuntimeException("Authorization not found"));
        return convertToDTO(auth);
    }
}