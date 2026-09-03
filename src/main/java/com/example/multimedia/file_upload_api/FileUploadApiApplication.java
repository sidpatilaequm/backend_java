package com.example.multimedia.file_upload_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileUploadApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileUploadApiApplication.class, args);
	}

}
