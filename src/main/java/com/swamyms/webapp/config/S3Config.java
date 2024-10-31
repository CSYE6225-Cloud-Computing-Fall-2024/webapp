package com.swamyms.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;

@Configuration
public class S3Config {
//    @Value("${aws.profile.name}")
//    private String awsProfileName;

    @Value("${aws.region}")
    private String awsRegion;


    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
//                .credentialsProvider(ProfileCredentialsProvider.create(awsProfileName)) // Passing the profile name from .env
                .region(Region.of(awsRegion)) // Passing the desired region from .env
                .build();
    }
}


