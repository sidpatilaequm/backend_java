package com.example.multimedia.file_upload_api.dto;

import java.util.HashMap;
import java.util.Map;

public class ServiceResponse {
    private String status;
    private String statusMsg;
    private String errorCode;
    private Map<String, Object> data = new HashMap<>();
    private String dataString = "";

    public ServiceResponse() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
    }
}
