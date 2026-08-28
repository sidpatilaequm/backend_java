package com.example.multimedia.file_upload_api;

import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.VendorMaster;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.VendorMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    @Autowired
    private com.example.multimedia.file_upload_api.repository.UserDetailRepository userDetailRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting DataMigrationRunner...");
        Optional<com.example.multimedia.file_upload_api.entity.UserDetail> userOpt = userDetailRepository.findByEmail("siddarthpatil17+2001@gmail.com");
        if (userOpt.isPresent()) {
            com.example.multimedia.file_upload_api.entity.UserDetail user = userOpt.get();
            user.setCompanyCode("1000");
            user.setPlantCode("1100");
            user.setPurchOrgCode("1000");
            userDetailRepository.save(user);
            System.out.println("Successfully updated UserDetail siddarthpatil17+2001@gmail.com with company 1000, plant 1100, purch org 1000.");
        } else {
            System.out.println("UserDetail siddarthpatil17+2001@gmail.com not found.");
        }
    }
}
