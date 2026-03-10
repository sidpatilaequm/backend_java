package com.example.multimedia.file_upload_api.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class CodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generateUniqueCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
} 