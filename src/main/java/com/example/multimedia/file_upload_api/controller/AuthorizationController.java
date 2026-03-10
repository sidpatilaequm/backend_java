package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.AuthorizationDTO;
import com.example.multimedia.file_upload_api.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/authorization")
public class AuthorizationController {

    @Autowired
    private AuthorizationService service;

    @PostMapping("/create")
    public ResponseEntity<AuthorizationDTO> create(@RequestBody AuthorizationDTO dto) {
        return ResponseEntity.ok(service.createAuthorization(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AuthorizationDTO> update(@PathVariable int id, @RequestBody AuthorizationDTO dto) {
        return ResponseEntity.ok(service.updateAuthorization(id, dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<AuthorizationDTO>> getAll() {
        return ResponseEntity.ok(service.getAllAuthorizations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorizationDTO> getById(@PathVariable int id) {
        return ResponseEntity.ok(service.getAuthorizationById(id));
    }
}
