package com.example.multimedia.file_upload_api.utils;

import java.util.Random;

public class PasswordUtils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    public static String generateRandomPassword(String name) {
        String cleanName = "User";
        if (name != null && !name.trim().isEmpty()) {
            cleanName = name.trim().replaceAll("[^a-zA-Z0-9]", "");
            if (cleanName.isEmpty()) {
                cleanName = "User";
            }
        }
        
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            randomPart.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        
        return cleanName + "@" + randomPart.toString();
    }
}
