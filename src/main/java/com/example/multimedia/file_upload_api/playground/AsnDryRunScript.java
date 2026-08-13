package com.example.multimedia.file_upload_api.playground;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple standalone script to Dry Run the ASN Creation API.
 * 
 * Instructions:
 * 1. Start your Spring Boot application so it's running on localhost:8080.
 * 2. Update the USER_ID, PO_ID, and VENDOR_BPNO below to match valid records in your MySQL database.
 * 3. Run this class's main method.
 */
public class AsnDryRunScript {

    // IMPORTANT: Update these values to match your database!
    private static final String USER_ID = "1";
    private static final String PO_ID = "PO-2026-1195";
    private static final String VENDOR_BPNO = "BP-MARK-01";
    
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 1. Create a dummy PDF file for the test
        File dummyFile = new File("dummy_test_file.pdf");
        if (!dummyFile.exists()) {
            Files.write(dummyFile.toPath(), "This is a dummy PDF file for testing ASN".getBytes());
        }

        // 2. Prepare the JSON Payload
        String jsonPayload = "{\n" +
                "  \"po_id\": \"" + PO_ID + "\",\n" +
                "  \"vendor_bpno\": \"" + VENDOR_BPNO + "\",\n" +
                "  \"shipment_details\": {\n" +
                "    \"invoice_number\": \"INV-" + UUID.randomUUID().toString().substring(0, 5) + "\",\n" +
                "    \"irn\": \"IRN987654321\",\n" +
                "    \"eway_bill\": \"EWB123\",\n" +
                "    \"ewb_valid_to\": \"2026-08-01\",\n" +
                "    \"vehicle_number\": \"KA-01-AB-1234\",\n" +
                "    \"transporter_code\": \"T-445\",\n" +
                "    \"lr_number\": \"LR-001\",\n" +
                "    \"dispatch_date\": \"2026-07-31\",\n" +
                "    \"expected_delivery\": \"2026-08-05\",\n" +
                "    \"packaging\": \"Returnable bin - RB-40\"\n" +
                "  },\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"line_number\": 10,\n" +
                "      \"part_number\": \"MAT-001\",\n" +
                "      \"quantity_shipped\": 1.0,\n" +
                "      \"batch_heat_number\": \"HT-102\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // 3. Build Multipart Request
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("asnData", null, RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .addFormDataPart("taxInvoiceAttached", dummyFile.getName(), RequestBody.create(dummyFile, MediaType.parse("application/pdf")))
                .addFormDataPart("ewayBillAttached", dummyFile.getName(), RequestBody.create(dummyFile, MediaType.parse("application/pdf")))
                .addFormDataPart("packingListAttached", dummyFile.getName(), RequestBody.create(dummyFile, MediaType.parse("application/pdf")))
                .build();

        // 4. Send the Request
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/vendor/asns")
                .post(requestBody)
                .addHeader("X-User-Id", USER_ID)
                .build();

        System.out.println("Sending Dry Run Request to http://localhost:8080/api/vendor/asns ...");
        
        try (Response response = client.newCall(request).execute()) {
            System.out.println("Response Code: " + response.code());
            if (response.body() != null) {
                System.out.println("Response Body: " + response.body().string());
            }
            if (response.isSuccessful()) {
                System.out.println("✅ Dry run successful! Check FolderIt to see the dynamically created folders.");
            } else {
                System.out.println("❌ Dry run failed. Ensure your database contains the matching PO_ID, USER_ID, and VENDOR_BPNO.");
            }
        }
    }
}
