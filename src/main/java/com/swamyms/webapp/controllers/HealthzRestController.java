package com.swamyms.webapp.controllers;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController

public class HealthzRestController {

    private static final Logger logger = LoggerFactory.getLogger(HealthzRestController.class);


    private final EntityManager entityManager;
    private final MeterRegistry meterRegistry;

    // Constructor for dependency injection
    @Autowired
    public HealthzRestController(EntityManager entityManager, MeterRegistry meterRegistry) {
        this.entityManager = entityManager;
        this.meterRegistry = meterRegistry;

        // Register your metrics here
        this.apiCallTimer = Timer.builder("api.healthz.calls")
                .description("Time taken for health check API calls")
                .register(meterRegistry);

        this.dbQueryTimer = Timer.builder("db.queries.execution")
                .description("Time taken for database queries")
                .register(meterRegistry);
    }

    private final Timer apiCallTimer;
    private final Timer dbQueryTimer;

    @GetMapping("/healthz")
    private ResponseEntity<?> getHealthzStatus(@RequestParam(required = false) HashMap<String, String> params, // Check for query parameters
                                               @RequestBody(required = false) String requestBody // Check for request body
    ) {
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");

        // Check if there are any query parameters or a request body
        if ((params != null && !params.isEmpty()) || (requestBody != null && !requestBody.isEmpty())) {
            logger.warn("Bad request: unexpected parameters or body present"); // Log warning
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).build();
        }
        long startTime = System.currentTimeMillis(); // Start timing API call
        try {
            Timer.Sample sample = Timer.start(meterRegistry); // Start Timer for the API call

            // Execute a simple query to check the health of the database
            long dbStartTime = System.currentTimeMillis(); // Start timing DB query
            Query query = entityManager.createNativeQuery("SELECT 1");
            query.getSingleResult();

            long dbEndTime = System.currentTimeMillis(); // End timing DB query

            // Record the time taken for the database query
            dbQueryTimer.record(dbEndTime - dbStartTime, TimeUnit.MILLISECONDS);

            // Record the API call duration
            sample.stop(apiCallTimer);

            logger.info("Health check successful. Time taken: {} ms", System.currentTimeMillis() - startTime); // Log info

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers).build();
        }catch (PersistenceException pe){
            logger.error("Database connection error: {}", pe.getMessage()); // Log error
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(headers).build();
        }
    }
    @PostMapping("/healthz")
    private ResponseEntity<String> handlePostHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        // Log the method call
        logger.error("POST /healthz called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @DeleteMapping("/healthz")
    private ResponseEntity<String> handleDeleteHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        logger.error("Delete /healthz called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();

    }
    @PutMapping("/healthz")
    private ResponseEntity<String> handlePutHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        logger.error("Put /healthz called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @PatchMapping("/healthz")
    private ResponseEntity<String> handlePatchHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        logger.error("Patch /healthz called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @RequestMapping(value = "/healthz",method = RequestMethod.HEAD)
    private ResponseEntity<String>  handleHeadHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        logger.error("HEAD /healthz called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }
    @RequestMapping(value = "/healthz",method = RequestMethod.OPTIONS)
    private ResponseEntity<String>  handleOptionsHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        logger.error("OPTIONS /healthz called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

}