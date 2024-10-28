package com.swamyms.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(ProfileCredentialsProvider.create("dev")) // Use your profile name here
                .region(Region.US_EAST_1) // Change this to your desired region
                .build();
    }
}


