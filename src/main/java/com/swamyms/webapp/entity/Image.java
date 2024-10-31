package com.swamyms.webapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @ReadOnlyProperty
    private String id;

    @ReadOnlyProperty
    @Column(name = "file_name")
    private String fileName;

    @ReadOnlyProperty
    @Column(name = "url")
    private String url;

    @ReadOnlyProperty
    @Column(name = "upload_date", updatable = false)
    private Date uploadDate;

//    @ReadOnlyProperty
//    @Column(name = "user_id")
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private String userId; // Assuming user_id is of type String

    @OneToOne // Establishing the one-to-one relationship with User
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false) // Ensuring user_id is not null
    private User user; // Reference to the User entity
    public Image() {

    }
}
