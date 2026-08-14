package com.example.multimedia.file_upload_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResult {
    private boolean verified;
    private String message;
    private List<VerifyDetail> details = new ArrayList<>();

    public static VerifyResult failed(String message) {
        return new VerifyResult(false, message, new ArrayList<>());
    }
}
