package com.swamyms.webapp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

//*This Pojo is only used to read json string provided in request payload*
//*As Jackson mapper ignores password property, it ignores both read and write functions*
//*Use this pojo so that Jackson mapper reads password from request body
@Getter
@Setter
public class AddUser {
    //Properties
    //These properties will not be stored in database or converted to Bean
    private String id;
    private String email;
    private String first_name;
    private String last_name;
    private String password;
    private LocalDateTime accountCreated;
    private LocalDateTime accountUpdated;
}
