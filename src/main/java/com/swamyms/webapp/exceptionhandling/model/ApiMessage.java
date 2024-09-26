package com.swamyms.webapp.exceptionhandling.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class ApiMessage {
    private int statusCode;
    private Date timestamp;
    private String message;
    private String description;
}
