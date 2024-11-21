package com.swamyms.webapp.service;

import com.swamyms.webapp.dao.UserDAO;
import com.swamyms.webapp.dao.VerifyUserDAO;
import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.entity.VerifyUser;
import com.swamyms.webapp.exceptionhandling.exceptions.UserNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;

@Service
public class VerifyUserService {

    @Autowired
    private VerifyUserDAO verifyUserDAO;

    private final Logger logger = LoggerFactory.getLogger(VerifyUserService.class); // Logger instance
    private final MeterRegistry meterRegistry; // Meter registry for metrics

    private final Timer dbQueryTimer; // Timer for authenticateUser method
    @Autowired
    public VerifyUserService(VerifyUserDAO theVerifyUserDao, MeterRegistry meterRegistry) {
        this.verifyUserDAO = theVerifyUserDao;
        this.meterRegistry = meterRegistry;


        this.dbQueryTimer = Timer.builder("db.user.queries.execution")
                .description("Time taken for User database queries")
                .register(meterRegistry);
    }

    public void addUser(VerifyUser user){
        long startTime = System.currentTimeMillis(); // Start timing API call
        logger.info("Adding user: {}", user.getUsername()); // Log saving user
        Timer.Sample sample = Timer.start(meterRegistry); // Start timing

        verifyUserDAO.save(user);

        sample.stop(dbQueryTimer); // Stop timing and record
        logger.info("User saved successfully in : {} ms", System.currentTimeMillis() - startTime); // Log success
    }

    public VerifyUser getByName(String email){


        Timer.Sample sample = Timer.start(meterRegistry); // Start timing
        long startTime = System.currentTimeMillis(); // Start timing API call
        logger.info("Fetching user by email: {}", email); // Log fetching user

        VerifyUser fetchedUser = verifyUserDAO.findByUsername(email);

        sample.stop(dbQueryTimer); // Stop timing and record
        logger.info("Get User By Email Time taken: {} ms", System.currentTimeMillis() - startTime); // Log info

        if (fetchedUser == null) {
            throw new UserNotFoundException("User with username '" + email + "' not found.");
        }
        return fetchedUser;
    }

    public boolean updateStatus(String username) {

        VerifyUser verifyUser = getByName(username);
        // Get instant from database and current instant
        Instant userInstant = verifyUser.getEmailSent();
        Instant currentInstant = Instant.now();
        logger.debug("userInstant TimeStamp = " + userInstant);
        logger.debug("currentInstant TimeStamp= " + currentInstant);

        Duration duration = Duration.between(userInstant, currentInstant);
        logger.debug("Duration: Duration in between userInstant and currentInstant = " + duration);
        long differenceInMinutes = Math.abs(duration.toMinutes());

        logger.debug("Verify User Service Debug: differenceInMinutes = " + differenceInMinutes);
        // Difference should be less than 2 minutes
        if(differenceInMinutes <= 2) {
            verifyUser.setVerified(true);
            verifyUserDAO.save(verifyUser);
            logger.info("Verify User Service Info : " + username + " has been verified successfully.");
            return true;
        } else {
            logger.error("Verify User Service Error: Verification link timed out for user: " + username);
        }

        return false;
    }
}
