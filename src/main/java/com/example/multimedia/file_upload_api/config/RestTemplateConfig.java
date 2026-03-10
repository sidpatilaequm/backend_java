package com.example.multimedia.file_upload_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Value("${attestr.auth.token}")
    private String authToken;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add authorization interceptor
        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set("Authorization", "Basic " + authToken);
            return execution.execute(request, body);
        };
        restTemplate.getInterceptors().add(interceptor);
        
        // Set custom error handler
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        
        return restTemplate;
    }
}
