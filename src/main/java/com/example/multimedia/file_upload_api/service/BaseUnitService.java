package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.BaseUnitDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.enums.BaseUnit;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseUnitService {

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    public ServiceResponse getAllBaseUnits() {
        ServiceResponse response = new ServiceResponse();

        try {
            List<BaseUnitDTO> baseUnits = Arrays.stream(BaseUnit.values())
                .map(unit -> new BaseUnitDTO(
                    unit.getCode(),
                    unit.getDescription(),
                    unit.getDisplayValue()
                ))
                .collect(Collectors.toList());

            response.addData("baseUnits", baseUnits);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Base units retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve base units: " + e.getMessage()
            );
        }
    }
} 