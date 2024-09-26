package com.swamyms.webapp.exceptionhandling;


import com.swamyms.webapp.exceptionhandling.exceptions.DataBaseConnectionException;
import com.swamyms.webapp.exceptionhandling.exceptions.MethodNotAllowedException;
import com.swamyms.webapp.exceptionhandling.model.ApiMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Date;

@RestControllerAdvice
public class ControllerExceptionHandler {

//    @ExceptionHandler(ResourceNotFoundException.class)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    public ApiMessage resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
//
//        return new ApiMessage(
//                HttpStatus.NOT_FOUND.value(),
//                new Date(),
//                ex.getMessage(),
//                request.getDescription(false));
//    }



    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ApiMessage resourceNotFoundException(NoResourceFoundException ex, WebRequest request) {

        return new ApiMessage(
                HttpStatus.NOT_FOUND.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
    }

    @ExceptionHandler(DataBaseConnectionException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public ApiMessage dataBaseConnectionException(DataBaseConnectionException ex, WebRequest request) {

        return new ApiMessage(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public ApiMessage methodNotAllowedException(MethodNotAllowedException ex, WebRequest request){
        return  new ApiMessage(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiMessage globalExceptionHandler(Exception ex, WebRequest request) {
        ex.printStackTrace();

        return new ApiMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
    }
}
