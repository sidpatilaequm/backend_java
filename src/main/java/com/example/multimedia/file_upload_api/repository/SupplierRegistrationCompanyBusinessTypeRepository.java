package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.SupplierRegistrationCompanyBusinessType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRegistrationCompanyBusinessTypeRepository
        extends JpaRepository<SupplierRegistrationCompanyBusinessType, SupplierRegistrationCompanyBusinessType.Pk> {

    List<SupplierRegistrationCompanyBusinessType> findByRegistrationId(Long registrationId);

    Optional<SupplierRegistrationCompanyBusinessType> findByRegistrationIdAndCompanyCode(Long registrationId, String companyCode);
}
