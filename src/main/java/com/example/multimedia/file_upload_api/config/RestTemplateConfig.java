package com.example.multimedia.file_upload_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Deliberately no global Authorization interceptor here — this RestTemplate bean is shared
    // across unrelated integrations (OpenAI OCR, Microvista, WorkFlow, Attestr-backed services),
    // each with its own auth scheme, and each already sets its own Authorization header
    // per-request. A prior global interceptor here unconditionally overwrote every outgoing
    // request's Authorization header with "Basic " + the Attestr token, silently breaking every
    // other caller (e.g. OpenAI's Bearer auth got replaced with Basic auth, causing 401s) while
    // being redundant for Attestr calls, which already set that same header themselves.
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        return restTemplate;
    }
}
