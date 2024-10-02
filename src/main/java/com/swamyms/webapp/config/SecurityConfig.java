package com.swamyms.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //Bcrypt password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //encode password with Bcrypt
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    //check if authorization password matches with Bcrypt password in database
    public boolean authenticatePassword(String userPassword, String hashPassword) {
        return passwordEncoder.matches(userPassword, hashPassword);
    }
}
