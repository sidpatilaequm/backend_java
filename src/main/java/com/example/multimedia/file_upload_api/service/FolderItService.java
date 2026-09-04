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

    // Previously hardcoded here; now admin-editable via PlatformCredentialService (System
    // Settings > FolderIT Integration) — these seed the DB with these exact values the first
    // time they're read, so nothing changes until someone actually edits them.
    private static final String DEFAULT_CLIENT_ID = "fPKfRJTOEyxFrNNH";
    private static final String DEFAULT_CLIENT_SECRET = "I8b5VmhC4Xdtfn-iqksE9r~HPu";
    private static final String DEFAULT_ACCOUNT_UID = "mprUk0ZilV";

    private static final String UID_QUOTATION = "dh4IV0mZFA";
    private static final String UID_VENDOR = "7KsvH0VM1V";
    private static final String UID_ASN = "5blVc0PTxf";
    private static final String UID_DEFAULT = "xsRm30xQB_";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final PlatformCredentialService credentials;

    public FolderItService(PlatformCredentialService credentials) {
        this.credentials = credentials;
    }

    private String clientId() { return credentials.get("folderit.client_id", DEFAULT_CLIENT_ID); }
    private String clientSecret() { return credentials.get("folderit.client_secret", DEFAULT_CLIENT_SECRET); }
    private String accountUid() { return credentials.get("folderit.account_uid", DEFAULT_ACCOUNT_UID); }

    /** Exposed so callers can build their own stable reference to a file (e.g. the FolderIt API
     *  download endpoint for a known file uid) without needing a fresh presigned URL up front. */
    public String getAccountUid() { return accountUid(); }

    private String getAccessToken() throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", clientId())
                .add("client_secret", clientSecret())
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
        String searchUrl = "https://api.folderit.com/v2/accounts/" + accountUid() + "/search/folders";
        
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
        String createUrl = "https://api.folderit.com/v2/accounts/" + accountUid() + "/folders";
        
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

    /**
     * @return the uploaded file's FolderIt UID (from the "complete" action's response),
     *         so callers can persist a reference and resolve a download link later.
     */
    public String uploadFileToFolderIt(MultipartFile file, String documentType, String vendorName, String monthYear) throws IOException {
        String token = getAccessToken();
        String folderUid = determineFolderUid(documentType);

        // Apply dynamic structure for quotation and ASN
        if (documentType != null && documentType.equalsIgnoreCase("quotation")) {
            // New Hierarchy: Purchase Orders (yhM_f0kYOd) -> [Month Year] -> SAP Quotation -> Process
            folderUid = "yhM_f0kYOd"; // Purchase Orders UID

            String targetMonthYear = monthYear;
            if (targetMonthYear == null || targetMonthYear.trim().isEmpty()) {
                // Generate current month year, e.g., "Aug 2026"
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
                targetMonthYear = now.format(formatter);
            }

            folderUid = getOrCreateFolder(token, folderUid, targetMonthYear.trim());
            folderUid = getOrCreateFolder(token, folderUid, "SAP Quotation");
            getOrCreateFolder(token, folderUid, "Archive"); // Just create the Archive folder as requested
            folderUid = getOrCreateFolder(token, folderUid, "Process"); // Set target to Process

        } else if (documentType != null && documentType.toUpperCase().startsWith("ASN")) {
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

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        return uploadBytesToFolder(file.getBytes(), file.getOriginalFilename(), contentType, folderUid, token);
    }

    /**
     * Finds or creates a folder for one applicant's documents directly under the shared
     * "vendor" root, named as given (e.g. a registration-id placeholder before approval,
     * "{vendorCode} - {vendorName}" after). Callers persist the returned uid and pass it to
     * {@link #uploadFileToFolder} for every subsequent document on the same application, and
     * to {@link #renameFolder} once a better name becomes available.
     */
    public String getOrCreateVendorFolder(String name) throws IOException {
        String token = getAccessToken();
        return getOrCreateFolder(token, UID_VENDOR, name);
    }

    /** Renames an existing folder — used to relabel a placeholder "REG-{id}" name with the real "{vendorCode} - {vendorName}" once approval assigns a vendor code. */
    public void renameFolder(String folderUid, String newName) throws IOException {
        String token = getAccessToken();
        renameResource(token, "folders", folderUid, newName);
    }

    /**
     * Fetches a fresh presigned S3 URL for viewing/downloading an uploaded file. FolderIt's
     * link expires in ~60 seconds, so this must be called on demand right before the caller
     * needs it (e.g. when an approver opens a request) — never stored ahead of time.
     */
    public String getDownloadUrl(String fileUid) throws IOException {
        String token = getAccessToken();
        Request req = new Request.Builder()
                .url("https://api.folderit.com/v2/accounts/" + accountUid() + "/files/" + fileUid + "/download")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        // FolderIt's /download response carries its own {"url": ...} JSON body but is ALSO a
        // redirect (3xx + Location: the S3 link). The shared httpClient follows redirects by
        // default, so it was swallowing FolderIt's JSON and returning S3's raw response instead
        // — hence the "must begin with '{'" parse failure. Use a non-redirecting client here so
        // we always see FolderIt's own response.
        OkHttpClient noRedirectClient = httpClient.newBuilder().followRedirects(false).followSslRedirects(false).build();
        try (Response response = noRedirectClient.newCall(req).execute()) {
            String location = response.header("Location");
            if (location != null && !location.isBlank()) {
                return location;
            }
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get download URL for " + fileUid + ": " + response.body().string());
            }
            JSONObject json = new JSONObject(response.body().string());
            return json.getString("url");
        }
    }

    public String findFirstExcelFileInFolder(String folderUid) throws IOException {
        String token = getAccessToken();
        String searchUrl = "https://api.folderit.com/v2/accounts/" + accountUid() + "/search/files";
        
        JSONObject searchPayload = new JSONObject();
        searchPayload.put("parentUid", folderUid);
        
        RequestBody searchBody = RequestBody.create(
                searchPayload.toString(),
                MediaType.parse("application/json")
        );
        
        Request searchReq = new Request.Builder()
                .url(searchUrl)
                .addHeader("Authorization", "Bearer " + token)
                .post(searchBody)
                .build();
                
        try (Response response = httpClient.newCall(searchReq).execute()) {
            if (response.isSuccessful()) {
                String resStr = response.body().string();
                JSONObject resJson = new JSONObject(resStr);
                if (resJson.has("files")) {
                    JSONArray files = resJson.getJSONArray("files");
                    for (int i = 0; i < files.length(); i++) {
                        JSONObject file = files.getJSONObject(i);
                        String name = file.optString("name", "").toLowerCase();
                        if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv")) {
                            return file.getString("uid");
                        }
                    }
                }
            }
        }
        return null;
    }

    public record DownloadedFile(byte[] bytes, String contentType) {}

    /**
     * Fetches the file's actual bytes (via the presigned URL from {@link #getDownloadUrl}) so the
     * caller can re-serve them with its own Content-Disposition — FolderIt's own presigned link
     * bakes in "attachment", which forces a download instead of an inline preview.
     */
    public DownloadedFile downloadFileBytes(String fileUid) throws IOException {
        String url = getDownloadUrl(fileUid);
        Request req = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file " + fileUid);
            }
            String contentType = response.header("Content-Type", "application/octet-stream");
            return new DownloadedFile(response.body().bytes(), contentType);
        }
    }

    private void renameResource(String token, String resourceType, String uid, String newName) throws IOException {
        JSONObject payload = new JSONObject().put("name", newName);
        Request req = new Request.Builder()
                .url("https://api.folderit.com/v2/accounts/" + accountUid() + "/" + resourceType + "/" + uid)
                .addHeader("Authorization", "Bearer " + token)
                .put(RequestBody.create(payload.toString(), MediaType.parse("application/json")))
                .build();
        try (Response response = httpClient.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Rename " + resourceType + " " + uid + " failed: " + response.body().string());
            }
        }
    }

    /** Uploads directly into a known folder — skips the documentType/vendorName folder-resolution logic above. */
    public String uploadFileToFolder(MultipartFile file, String folderUid) throws IOException {
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        return uploadBytesToFolder(file.getBytes(), file.getOriginalFilename(), contentType, folderUid, getAccessToken());
    }

    /** Same as {@link #uploadFileToFolder(MultipartFile, String)} but for bytes generated
     *  server-side (e.g. a report) rather than something a user uploaded as a MultipartFile. */
    public String uploadBytesToFolder(byte[] bytes, String fileName, String contentType, String folderUid) throws IOException {
        return uploadBytesToFolder(bytes, fileName, contentType, folderUid, getAccessToken());
    }

    private String uploadBytesToFolder(byte[] bytes, String fileName, String contentType, String folderUid, String token) throws IOException {
        String authHeader = "Bearer " + token;
        String baseUrl = "https://api.folderit.com/v2/accounts/" + accountUid() + "/files/upload";
        long fileSize = bytes.length;

        // 1. Create Action
        JSONObject createPayload = new JSONObject();
        createPayload.put("action", "create");
        createPayload.put("folderUid", folderUid);
        createPayload.put("fileName", fileName);
        createPayload.put("fileSize", fileSize);
        createPayload.put("contentType", contentType);

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
        partPayload.put("contentLength", fileSize);

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
                bytes,
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
        completePayload.put("fileSize", fileSize);
        completePayload.put("fileName", fileName);
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
            String resStr = response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Complete action failed: " + resStr);
            }
            logger.info("File uploaded successfully to FolderIt: {}", fileName);
            JSONObject resJson = new JSONObject(resStr);
            if (resJson.has("uid")) return resJson.getString("uid");
            if (resJson.has("fileUid")) return resJson.getString("fileUid");
            if (resJson.has("id")) return resJson.getString("id");
            if (resJson.has("recordUid")) return resJson.getString("recordUid");
            return key; // fall back to the upload key if no explicit id field comes back
        }
    }
}
