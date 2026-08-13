package com.example.multimedia.file_upload_api.playground;

import com.example.multimedia.file_upload_api.service.FolderItService;

public class FolderItDebug {
    public static void main(String[] args) {
        try {
            System.out.println("Starting FolderIt debug...");
            FolderItService folderItService = new FolderItService();
            
            // Expose getAccessToken and getOrCreateFolder using reflection since they are private
            java.lang.reflect.Method getAccessTokenMethod = FolderItService.class.getDeclaredMethod("getAccessToken");
            getAccessTokenMethod.setAccessible(true);
            String token = (String) getAccessTokenMethod.invoke(folderItService);
            System.out.println("Token acquired successfully.");

            java.lang.reflect.Method getOrCreateFolderMethod = FolderItService.class.getDeclaredMethod("getOrCreateFolder", String.class, String.class, String.class);
            getOrCreateFolderMethod.setAccessible(true);
            
            String parentUid = "5blVc0PTxf"; // ASN Folder UID
            String folderName = "Test_Vendor_123";
            
            System.out.println("Attempting to create folder under " + parentUid + " with name: " + folderName);
            String newFolderUid = (String) getOrCreateFolderMethod.invoke(folderItService, token, parentUid, folderName);
            
            System.out.println("Success! New folder UID: " + newFolderUid);
        } catch (Exception e) {
            System.err.println("Error occurred during FolderIt interaction:");
            e.printStackTrace();
        }
    }
}
