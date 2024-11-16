package com.swamyms.webapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Setter
@Getter
@Entity(name = "verify_user")
@Table(name="verify_user")
@Component
public class VerifyUser {
    @Id
    @UuidGenerator
    private String id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Getter
    @Column(name = "verified")
    private boolean verified;

    @Column(name = "email_sent")
    @CreationTimestamp
    private Instant emailSent;

    @Column(name = "verify_email_sent")
    private boolean verifyEmailSent;

    public VerifyUser() {}

    public VerifyUser(String username) {
        this.username = username;
        this.verified = false;
        this.verifyEmailSent = false;
    }

}
