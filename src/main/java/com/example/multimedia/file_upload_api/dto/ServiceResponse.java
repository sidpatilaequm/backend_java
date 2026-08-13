package com.example.multimedia.file_upload_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponse {
    private String status;
    private String statusMsg;
    private String errorCode;

    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    @Builder.Default
    private String dataString = "";

    // Legacy helper
    public void addData(String key, Object value) {
        if (this.data == null) this.data = new HashMap<>();
        this.data.put(key, value);
    }

    // Custom Builder methods to support the new style
    public static class ServiceResponseBuilder {
        private String status;
        private String statusMsg;
        private Map<String, Object> data;

        public ServiceResponseBuilder status(boolean success) {
            this.status = success ? "SUCCESS" : "ERROR";
            return this;
        }

        public ServiceResponseBuilder message(String msg) {
            this.statusMsg = msg;
            return this;
        }

        // Maps a single object to the "result" key in the data map
        public ServiceResponseBuilder data(Object obj) {
            if (this.data == null) this.data = new HashMap<>();
            this.data.put("result", obj);
            return this;
        }
    }
}
