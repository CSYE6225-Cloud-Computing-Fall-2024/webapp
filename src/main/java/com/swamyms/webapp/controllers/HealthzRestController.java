package com.swamyms.webapp.controllers;


import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/healthz")

public class HealthzRestController {

    @Autowired
    private final EntityManager entityManager;
    private final MeterRegistry meterRegistry;

    public HealthzRestController(EntityManager entityManager, MeterRegistry meterRegistry) {
        this.entityManager = entityManager;
        this.meterRegistry = meterRegistry;
    }

    @Timed(value = "api.healthz.get", description = "Time taken to check healthz status")
    @GetMapping
    private ResponseEntity<?> getHealthzStatus(@RequestParam(required = false) HashMap<String, String> params, // Check for query parameters
                                                    @RequestBody(required = false) String requestBody // Check for request body
    ) {
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");

        // Track the API call count
        meterRegistry.counter("api.healthz.get.call.count").increment();

        // Check if there are any query parameters or a request body
        if ((params != null && !params.isEmpty()) || (requestBody != null && !requestBody.isEmpty())) {
            // Return 400 Bad Request if any query parameters or request body is present
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).build();
//                    .body(errorResponse);
        }
        try {
            Timer.Sample sample = Timer.start(meterRegistry); // Start timer for DB query
            // Execute a simple query to check the health of the database
            Query query = entityManager.createNativeQuery("SELECT 1");
            query.getSingleResult();
            sample.stop(meterRegistry.timer("db.healthz.query.time")); // Stop timer for DB query

            // Return 200 OK with cache control headers
//            ApiMessage successResponse = new ApiMessage(
//                    HttpStatus.OK.value(),
//                    new Date(),
//                    "Good Request",
//                    "Successfully Get Request Executed for Healthz Endpoint"
//            );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers).build();
        }catch (PersistenceException pe){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(headers).build();
        }
    }
    @PostMapping
    private ResponseEntity<String> handlePostHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @DeleteMapping
    private ResponseEntity<String> handleDeleteHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();

    }
    @PutMapping
    private ResponseEntity<String> handlePutHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @PatchMapping
    private ResponseEntity<String> handlePatchHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @RequestMapping(method = RequestMethod.HEAD)
    private ResponseEntity<String>  handleHeadHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }
    @RequestMapping(method = RequestMethod.OPTIONS)
    private ResponseEntity<String>  handleOptionsHealthzStatus(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

}
