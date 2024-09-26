package com.swamyms.webapp.controllers;


import com.swamyms.webapp.exceptionhandling.exceptions.DataBaseConnectionException;
import com.swamyms.webapp.exceptionhandling.model.ApiMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/healthz")

public class HealthzRestController {

    @Autowired
    private final EntityManager entityManager;

    public HealthzRestController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping
    private ResponseEntity<?> getHealthzStatus(@RequestParam(required = false) HashMap<String, String> params, // Check for query parameters
                                                    @RequestBody(required = false) String requestBody // Check for request body
    ) {
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");

        // Check if there are any query parameters or a request body
        if ((params != null && !params.isEmpty()) || (requestBody != null && !requestBody.isEmpty())) {
            // Return 400 Bad Request if any query parameters or request body is present
            ApiMessage errorResponse = new ApiMessage(
                    HttpStatus.BAD_REQUEST.value(),
                    new Date(),
                    "Bad Request",
                    "No parameters or request body allowed."
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers)
                    .body(errorResponse);
        }
        try {
            // Execute a simple query to check the health of the database
            Query query = entityManager.createNativeQuery("SELECT 1");
            query.getSingleResult();

            // Return 200 OK with cache control headers
            ApiMessage successResponse = new ApiMessage(
                    HttpStatus.OK.value(),
                    new Date(),
                    "Good Request",
                    "Successfully Get Request Executed for Healthz Endpoint"
            );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .body(successResponse);
        }catch (PersistenceException pe){
            throw new DataBaseConnectionException("Data base connection failed.");
        }
    }

}
