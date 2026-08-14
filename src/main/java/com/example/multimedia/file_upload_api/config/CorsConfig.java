package com.example.multimedia.file_upload_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://13.201.75.122:5173", "http://localhost:5173", "http://localhost:3000",
                        "http://ec2-13-232-238-193.ap-south-1.compute.amazonaws.com:5173", "http://13.232.238.193:5173",
                        "http://ec2-13-232-238-193.ap-south-1.compute.amazonaws.com", "http://13.232.238.193",
                        "https://nexdsupportal.in", "http://nexdsupportal.in")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
