package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Currency;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    List<Currency> findBySuperAdmin(SuperAdmin superAdmin);

    Optional<Currency> findByCurrencyCode(String currencyCode);

    Optional<Currency> findByCurrencyCodeAndSuperAdmin(String currencyCode, SuperAdmin superAdmin);

    boolean existsByCurrencyCodeAndSuperAdmin(String currencyCode, SuperAdmin superAdmin);
}
