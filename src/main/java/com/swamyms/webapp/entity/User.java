package com.swamyms.webapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "webapp_user")
public class User {

    //define fields

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "password", nullable = false)
//    @ToString.Exclude
    @JsonIgnore //ignore password property from Jackson mapper
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @ReadOnlyProperty
    @Column(name = "account_created", updatable = false)
    private LocalDateTime accountCreated;

    @ReadOnlyProperty
    @Column(name = "account_updated")
    private LocalDateTime accountUpdated;


    //define constructors

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        accountCreated = LocalDateTime.now();
        accountUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        accountUpdated = LocalDateTime.now();
    }


}
