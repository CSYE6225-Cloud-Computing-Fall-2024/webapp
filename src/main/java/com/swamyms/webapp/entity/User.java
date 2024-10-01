package com.swamyms.webapp.entity;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "user")
public class User {

    //define fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "password")
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "account_created")
    private Date accountCreated;
    @Column(name = "account_updated")
    private Date accountUpdated;


    //define constructors

    public User() {
    }

    public User(String firstName, String lastName, String password, String email, Date accountCreated, Date accountUpdated) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.accountCreated = accountCreated;
        this.accountUpdated = accountUpdated;
    }

    //define getters//setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getAccountCreated() {
        return accountCreated;
    }

    public void setAccountCreated(Date accountCreated) {
        this.accountCreated = accountCreated;
    }

    public Date getAccountUpdated() {
        return accountUpdated;
    }

    public void setAccountUpdated(Date accountUpdated) {
        this.accountUpdated = accountUpdated;
    }


    //define toString()
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", accountCreated=" + accountCreated +
                ", accountUpdated=" + accountUpdated +
                '}';
    }
}
