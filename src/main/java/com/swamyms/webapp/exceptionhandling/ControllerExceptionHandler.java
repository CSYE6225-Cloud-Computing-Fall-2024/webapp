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
    public void resourceNotFoundException(NoResourceFoundException ex) throws NoResourceFoundException {

        throw ex;
    }

    @ExceptionHandler(DataBaseConnectionException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void dataBaseConnectionException(DataBaseConnectionException ex, WebRequest request) {

        throw ex;
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public void methodNotAllowedException(MethodNotAllowedException ex, WebRequest request){
        throw ex;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public void globalExceptionHandler(Exception ex, WebRequest request) throws Exception {
        ex.printStackTrace();

        throw ex;
    }
}
