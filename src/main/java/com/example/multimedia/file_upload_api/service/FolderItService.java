package com.example.multimedia.file_upload_api.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class FolderItService {

    private static final Logger logger = LoggerFactory.getLogger(FolderItService.class);

    private static final String CLIENT_ID = "fPKfRJTOEyxFrNNH";
    private static final String CLIENT_SECRET = "I8b5VmhC4Xdtfn-iqksE9r~HPu";
    private static final String ACCOUNT_UID = "mprUk0ZilV";

    private static final String UID_QUOTATION = "dh4IV0mZFA";
    private static final String UID_VENDOR = "7KsvH0VM1V";
    private static final String UID_ASN = "5blVc0PTxf";
    private static final String UID_DEFAULT = "xsRm30xQB_";

    private final OkHttpClient httpClient = new OkHttpClient();

    private String getAccessToken() throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .build();

        Request request = new Request.Builder()
                .url("https://auth.folderit.com/oauth2/token")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get token from FolderIt: " + response.body().string());
            }
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            return json.getString("access_token");
        }
    }

    private String determineFolderUid(String documentType) {
        if (documentType == null) return UID_DEFAULT;
        if (documentType.equalsIgnoreCase("quotation")) {
            return UID_QUOTATION;
        } else if (documentType.equalsIgnoreCase("vendor")) {
            return UID_VENDOR;
        } else if (documentType.toUpperCase().startsWith("ASN")) {
            return UID_ASN;
        }
        return UID_DEFAULT;
    }

    private String getOrCreateFolder(String token, String parentUid, String folderName) throws IOException {
        String authHeader = "Bearer " + token;
        
        // 1. Search for existing folder
        String searchUrl = "https://api.folderit.com/v2/accounts/" + ACCOUNT_UID + "/search/folders";
        
        JSONObject searchPayload = new JSONObject();
        searchPayload.put("parentUid", parentUid);
        
        RequestBody searchBody = RequestBody.create(
                searchPayload.toString(),
                MediaType.parse("application/json")
        );
        
        Request searchReq = new Request.Builder()
                .url(searchUrl)
                .addHeader("Authorization", authHeader)
                .post(searchBody)
                .build();
                
        try (Response response = httpClient.newCall(searchReq).execute()) {
            if (response.isSuccessful()) {
                String resStr = response.body().string();
                JSONObject resJson = new JSONObject(resStr);
                if (resJson.has("folders")) {
                    JSONArray folders = resJson.getJSONArray("folders");
                    for (int i = 0; i < folders.length(); i++) {
                        JSONObject folder = folders.getJSONObject(i);
                        if (folder.has("name") && folder.getString("name").equalsIgnoreCase(folderName.trim())) {
                            logger.info("Found existing folder for {}: {}", folderName, folder.getString("uid"));
                            return folder.getString("uid");
                        }
                    }
                }
            }
        }
        
        // 2. If not found, create it
        String createUrl = "https://api.folderit.com/v2/accounts/" + ACCOUNT_UID + "/folders";
        
        JSONObject createPayload = new JSONObject();
        createPayload.put("parentUid", parentUid);
        createPayload.put("title", folderName); // Some APIs use title
        createPayload.put("name", folderName);  // V2 API expects name

        RequestBody createBody = RequestBody.create(
                createPayload.toString(),
                MediaType.parse("application/json")
        );
        
        Request createReq = new Request.Builder()
                .url(createUrl)
                .addHeader("Authorization", authHeader)
                .post(createBody)
                .build();
                
        try (Response response = httpClient.newCall(createReq).execute()) {
            String resStr = response.body().string();
            if (response.isSuccessful()) {
                JSONObject resJson = new JSONObject(resStr);
                if (resJson.has("uid")) return resJson.getString("uid");
                if (resJson.has("id")) return resJson.getString("id");
                if (resJson.has("recordUid")) return resJson.getString("recordUid");
            }
            
            // Fallback to v1 API which the user's C# code used if V2 API fails
            RequestBody formBody = new FormBody.Builder()
                .add("parentId", parentUid)
                .add("name", folderName)
                .build();
            Request v1Req = new Request.Builder()
                .url("https://api.folderit.com/folders")
                .addHeader("Authorization", authHeader)
                .post(formBody)
                .build();
            try (Response v1Res = httpClient.newCall(v1Req).execute()) {
                String v1Str = v1Res.body().string();
                if (v1Res.isSuccessful()) {
                    JSONObject v1Json = new JSONObject(v1Str);
                    if (v1Json.has("uid")) return v1Json.getString("uid");
                }
            }
            
            throw new IOException("Could not create/resolve folder " + folderName + ": " + resStr);
        }
    }

    public void uploadFileToFolderIt(MultipartFile file, String documentType, String vendorName, String monthYear) throws IOException {
        String token = getAccessToken();
        String folderUid = determineFolderUid(documentType);
        
        // Apply dynamic structure for quotation and ASN
        if (documentType != null && (documentType.equalsIgnoreCase("quotation") || documentType.toUpperCase().startsWith("ASN"))) {
            if (vendorName != null && !vendorName.trim().isEmpty()) {
                folderUid = getOrCreateFolder(token, folderUid, vendorName.trim());
                
                String targetMonthYear = monthYear;
                if (targetMonthYear == null || targetMonthYear.trim().isEmpty()) {
                    // Generate current month year, e.g., "Mar 2026"
                    LocalDate now = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
                    targetMonthYear = now.format(formatter);
                }
                
                folderUid = getOrCreateFolder(token, folderUid, targetMonthYear.trim());
            }
        }
        
        String authHeader = "Bearer " + token;
        String baseUrl = "https://api.folderit.com/v2/accounts/" + ACCOUNT_UID + "/files/upload";
        
        // 1. Create Action
        JSONObject createPayload = new JSONObject();
        createPayload.put("action", "create");
        createPayload.put("folderUid", folderUid);
        createPayload.put("fileName", file.getOriginalFilename());
        createPayload.put("fileSize", file.getSize());
        createPayload.put("contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        
        RequestBody createBody = RequestBody.create(
                createPayload.toString(),
                MediaType.parse("application/json")
        );
        
        Request createReq = new Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", authHeader)
                .post(createBody)
                .build();
                
        String uploadId;
        String key;
        try (Response response = httpClient.newCall(createReq).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Create action failed: " + response.body().string());
            }
            JSONObject resJson = new JSONObject(response.body().string());
            uploadId = resJson.getString("uploadId");
            key = resJson.getString("key");
        }
        
        // 2. Part Action
        JSONObject partPayload = new JSONObject();
        partPayload.put("action", "part");
        partPayload.put("uploadId", uploadId);
        partPayload.put("key", key);
        partPayload.put("partNumber", 1);
        partPayload.put("contentLength", file.getSize());
        
        RequestBody partBody = RequestBody.create(
                partPayload.toString(),
                MediaType.parse("application/json")
        );
        
        Request partReq = new Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", authHeader)
                .post(partBody)
                .build();
                
        String uploadUrl;
        try (Response response = httpClient.newCall(partReq).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Part action failed: " + response.body().string());
            }
            JSONObject resJson = new JSONObject(response.body().string());
            uploadUrl = resJson.getString("url");
        }
        
        // 3. Put File Action (S3 upload)
        RequestBody fileBody = RequestBody.create(
                file.getBytes(),
                MediaType.parse("application/octet-stream")
        );
        
        Request putReq = new Request.Builder()
                .url(uploadUrl)
                .put(fileBody)
                .build();
                
        try (Response response = httpClient.newCall(putReq).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("File PUT action failed: " + response.body().string());
            }
        }
        
        // 4. Complete Action
        JSONObject completePayload = new JSONObject();
        completePayload.put("action", "complete");
        completePayload.put("uploadId", uploadId);
        completePayload.put("key", key);
        completePayload.put("fileSize", file.getSize());
        completePayload.put("fileName", file.getOriginalFilename());
        completePayload.put("folderUid", folderUid);
        
        RequestBody completeBody = RequestBody.create(
                completePayload.toString(),
                MediaType.parse("application/json")
        );
        
        Request completeReq = new Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", authHeader)
                .post(completeBody)
                .build();
                
        try (Response response = httpClient.newCall(completeReq).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Complete action failed: " + response.body().string());
            }
            logger.info("File uploaded successfully to FolderIt: {}", file.getOriginalFilename());
        }
    }
}
