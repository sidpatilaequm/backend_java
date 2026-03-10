package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, Integer> {

    Optional<Authorization> findByAuthKey(String authKey);

    Optional<Authorization> findByAuthKeyIgnoreCase(String authKey);
}