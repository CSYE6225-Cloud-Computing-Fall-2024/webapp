package com.swamyms.webapp.service;

import com.swamyms.webapp.config.SecurityConfig;
import com.swamyms.webapp.dao.UserDAO;
import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.exceptionhandling.exceptions.DataBaseConnectionException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    //Inject UserDAO
    private UserDAO userDAO;

    @Autowired
    private SecurityConfig securityConfig;

    //Constructor Injection
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class); // Logger instance
    private final MeterRegistry meterRegistry; // Meter registry for metrics

    private final Timer dbQueryTimer; // Timer for authenticateUser method
    // Constructor Injection
    @Autowired
    public UserServiceImpl(UserDAO theUserDAO, MeterRegistry meterRegistry) {
        this.userDAO = theUserDAO;
        this.meterRegistry = meterRegistry;


        this.dbQueryTimer = Timer.builder("db.user.queries.execution")
                .description("Time taken for User database queries")
                .register(meterRegistry);
    }

    @Transactional
    @Override
    public User save(User theUser) {
        long startTime = System.currentTimeMillis(); // Start timing API call
        logger.info("Saving user: {}", theUser.getEmail()); // Log saving user
        Timer.Sample sample = Timer.start(meterRegistry); // Start timing

        User savedUser = userDAO.save(theUser); // Save user

        sample.stop(dbQueryTimer); // Stop timing and record
        logger.info("User saved successfully in : {} ms", System.currentTimeMillis() - startTime); // Log success
        return savedUser;

    }

    @Override
    public User getUserByEmail(String email) {
        Timer.Sample sample = Timer.start(meterRegistry); // Start timing
        long startTime = System.currentTimeMillis(); // Start timing API call
        logger.info("Fetching user by email: {}", email); // Log fetching user

        User getUserEmail = userDAO.findByEmail(email);; // Save user

        sample.stop(dbQueryTimer); // Stop timing and record
        logger.info("Get User By Email Time taken: {} ms", System.currentTimeMillis() - startTime); // Log info
        return getUserEmail;
    }

    public boolean authenticateUser(String email, String password) {
        logger.info("Authenticating user: {}", email); // Log authentication attempt
        Timer.Sample sample = Timer.start(meterRegistry); // Start timing
        long startTime = System.currentTimeMillis(); // Start timing API call

        try {
            User user = userDAO.findByEmail(email);
            if (user == null){
                logger.warn("User not found: {}", email); // Log warning if user not found
                return false;
            }

            String dbPassword = user.getPassword();
            boolean authenticated = securityConfig.authenticatePassword(password, dbPassword);
            sample.stop(dbQueryTimer); // Stop timing and record
            logger.info("User authentication successful in : {} ms", System.currentTimeMillis() - startTime); // Log success
//            logger.info("Get User By Email Time taken: {} ms", System.currentTimeMillis() - startTime); // Log info
            return authenticated;
        }catch (PersistenceException ex){
            logger.error("Database connection error during authentication for user: {}", email, ex); // Log error
            throw new DataBaseConnectionException();
        }
    }
}
