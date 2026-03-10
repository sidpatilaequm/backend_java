package com.example.multimedia.file_upload_api.playground.service;

import com.example.multimedia.file_upload_api.playground.dto.GSTResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GSTInsightsService {

    private static final String BASE_URL = "https://gst-insights-api.p.rapidapi.com";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.host}")
    private String rapidApiHost;

    public Object getGSTDetailsByGSTNumber(String gstNumber) {
        try {
            Request request = new Request.Builder()
                .url(BASE_URL + "/getGSTDetailsUsingGST/" + gstNumber)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", rapidApiHost)
                .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching GST details: " + e.getMessage());
        }
    }

    public Object getGSTDetailsByPAN(String panNumber) {
        try {
            Request request = new Request.Builder()
                .url(BASE_URL + "/getGSTDetailsUsingPAN/" + panNumber)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", rapidApiHost)
                .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching GST details by PAN: " + e.getMessage());
        }
    }

    public Object getGSTDetailsByCompanyName(String companyName) {
        try {
            Request request = new Request.Builder()
                .url(BASE_URL + "/getGSTDetailsUsingCompanyName/" + companyName)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", rapidApiHost)
                .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching GST details by company name: " + e.getMessage());
        }
    }

    public Object validateGSTNumber(String gstNumber) {
        try {
            Request request = new Request.Builder()
                .url(BASE_URL + "/validateGSTNumber/" + gstNumber)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", rapidApiHost)
                .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error validating GST number: " + e.getMessage());
        }
    }

    public Object getGSTStatus(String gstNumber) {
        try {
            Request request = new Request.Builder()
                .url(BASE_URL + "/getGSTStatus/" + gstNumber)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", rapidApiHost)
                .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching GST status: " + e.getMessage());
        }
    }

    public Object getGSTReturnFilingStatus(String gstNumber) {
        try {
            Request request = new Request.Builder()
                .url(BASE_URL + "/getGSTReturnFilingStatus/" + gstNumber)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", rapidApiHost)
                .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching GST return filing status: " + e.getMessage());
        }
    }

    public Object getGSTReturnFilingStatusForYear(String gstNumber, String year) {
        try {
            Request request = new Request.Builder()
                .url(BASE_URL + "/getGSTReturnFilingStatusSpecificYear/" + gstNumber + "/" + year)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", rapidApiHost)
                .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching GST return filing status for year: " + e.getMessage());
        }
    }

    public Object getAddressByGSTNumber(String gstNumber) {
        try {
            Request request = new Request.Builder()
                .url(BASE_URL + "/getAddressUsingGST/" + gstNumber)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", rapidApiHost)
                .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching address using GST number: " + e.getMessage());
        }
    }
} 