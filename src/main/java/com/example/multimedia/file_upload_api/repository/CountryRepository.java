package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Country;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findBySuperAdmin(SuperAdmin superAdmin);

    Optional<Country> findByCountryName(String countryName);

    Optional<Country> findByCountryNameAndSuperAdmin(String countryName, SuperAdmin superAdmin);

    boolean existsByCountryNameAndSuperAdmin(String countryName, SuperAdmin superAdmin);
}
