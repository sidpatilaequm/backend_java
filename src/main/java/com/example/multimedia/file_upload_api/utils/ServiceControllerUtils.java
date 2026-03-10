package com.example.multimedia.file_upload_api.utils;


import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import org.springframework.stereotype.Service;

@Service
public class ServiceControllerUtils {

    public ServiceResponse prepareMobileResponseSuccessStatus(ServiceResponse mresponse, String successCode, String statusMsg) {
        mresponse.setStatus(successCode);
        mresponse.setErrorCode("0");
        mresponse.setStatusMsg(statusMsg);
        return mresponse;
    }

    public ServiceResponse prepareMobileResponseErrorStatus(ServiceResponse mresponse, String errorCode, String statusMsg) {
        mresponse.setStatus(errorCode);
        mresponse.setErrorCode(errorCode);
        mresponse.setStatusMsg(statusMsg);
        return mresponse;
    }

    public ServiceResponse prepareMobileResponseInvalidData(ServiceResponse mresponse, String statusMsg) {
        mresponse.setStatus(AppConstants.INVALIDCODE);
        mresponse.setStatusMsg(statusMsg);
        mresponse.setErrorCode(AppConstants.INVALID_ERROR_CODE);
        return mresponse;
    }

    public ServiceResponse prepareMobileResponseSessionInvalidStatus(ServiceResponse mresponse, String errorCode, String statusMsg) {
        mresponse.setStatus(AppConstants.SESSION_INVALID_CODE);
        mresponse.setStatusMsg(statusMsg);
        mresponse.setErrorCode(errorCode);
        return mresponse;
    }

    public ServiceResponse prepareMobileResponseSuccessStatusUser(ServiceResponse mresponse, String successCode, String statusMsg) {
        mresponse.setStatus(successCode);
        mresponse.setStatusMsg(statusMsg);
        mresponse.setErrorCode("0");
        return mresponse;
    }

    public ServiceResponse prepareMobileResponseErrorStatusUser(ServiceResponse mresponse, String errorCode, String statusMsg) {
        mresponse.setStatus(errorCode);
        mresponse.setStatusMsg(statusMsg);
        mresponse.setErrorCode(errorCode);
        return mresponse;
    }
}
