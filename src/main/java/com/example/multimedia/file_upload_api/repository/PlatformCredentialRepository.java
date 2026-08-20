package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.PlatformCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformCredentialRepository extends JpaRepository<PlatformCredential, Long> {
    Optional<PlatformCredential> findByCredentialKey(String credentialKey);
    List<PlatformCredential> findByCredentialKeyStartingWith(String prefix);
}
