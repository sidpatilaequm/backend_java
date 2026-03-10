package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.MaterialTypeDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.enums.MaterialType;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaterialTypeService {

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    public ServiceResponse getAllMaterialTypes() {
        ServiceResponse response = new ServiceResponse();

        try {
            List<MaterialTypeDTO> materialTypes = Arrays.stream(MaterialType.values())
                .map(type -> new MaterialTypeDTO(
                    type.getCode(),
                    type.getDescription(),
                    type.getDisplayValue()
                ))
                .collect(Collectors.toList());

            response.addData("materialTypes", materialTypes);
            return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response,
                AppConstants.SUCCESSCODE,
                "Material types retrieved successfully"
            );

        } catch (Exception e) {
            return serviceControllerUtils.prepareMobileResponseErrorStatus(
                response,
                AppConstants.ERRORCODE,
                "Failed to retrieve material types: " + e.getMessage()
            );
        }
    }
} 