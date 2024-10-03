package com.swamyms.webapp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.swamyms.webapp.entity.AddUser;
import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.exceptionhandling.model.ApiMessage;
import com.swamyms.webapp.service.UserService;
import com.swamyms.webapp.validations.UserValidations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/v1")
public class UserRestController {

    private UserService userService;
    @Autowired
    private UserValidations userValidations;
//    private UserDAO userDAO;
    //quick and dirty method inject userDAO(Use Constructor Injection)
    public UserRestController(UserService theUserService){
        userService = theUserService;
    }

    //expose "/users" and return list of users
//    @GetMapping("/users")
//    public List<User> findAll(){
//        return userService.findAll();
//    }


    @GetMapping("/users")
    public ResponseEntity<Object> getUser(@RequestParam(required = false) HashMap<String, String> param, @RequestHeader(required = false) HttpHeaders headers, @RequestBody(required = false) String userBody) {
        //if params are present or body is not present return bad request
        if(param.size() > 0 || userBody != null) {
//            logger.error("User Get error: Params are present or body is not null");
            // Return 400 Bad Request if any query parameters or request body is present
            ApiMessage errorResponse = new ApiMessage(
                    HttpStatus.BAD_REQUEST.value(),
                    new Date(),
                    "Bad Request",
                    "No parameters or request body allowed."
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers)
                    .body(errorResponse);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }

        //get user credentials from header and check authentication
        String[] userCreds = getCreds(headers);

        //if user provides only username or password, or does not provides any credential, return bad request
        if(userCreds.length < 2) {
//            logger.error("User Get error: Enter both username and password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }

        boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
        if(!checkUserPassword) {
//            logger.error("User Get error: Unauthorized Access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
        }

        //retrieve user from db
        User user = userService.getUserByEmail(userCreds[0]);
//        debugLogger.debug("User Get Debug: User retrieved from database: " + user.getUsername());

        //use Jackson mapper to convert pojo class to json string
        try {
            ObjectMapper mapper = configureMapper();
            String jsonString = mapper.writeValueAsString(user);
//            debugLogger.debug("User Get Debug: Json String from request body: " + jsonString);
//            infoLogger.info("User Get: User Found: " + user.getUsername());
            return ResponseEntity.status(HttpStatus.OK).cacheControl(CacheControl.noCache()).contentType(MediaType.APPLICATION_JSON).body(jsonString);
        } catch(JsonProcessingException e) {
//            logger.error("User Get error: " + e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }
    }
    //add mapping for POST / users - add new user
    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestParam(required = false) HashMap<String, String> param, @RequestHeader(required = false) HttpHeaders headers, @RequestBody(required = false) String userBody) {


        //if params are present or body is not present return bad request
        if (param.size() > 0 || userBody == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }

        //get user credentials from header, if present return bad request
        String[] userCreds = getCreds(headers);
        if (userCreds.length > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }

        //configure Jackson mapper to read json string from request body
        try {
            ObjectMapper mapper = configureMapper();

            AddUser queryUser = mapper.readValue(userBody, AddUser.class);

            if(queryUser != null) {
                //check if any required property or value is missing in request body
                if(queryUser.getEmail() == null || queryUser.getPassword() == null ||
                        queryUser.getFirst_name() == null || queryUser.getLast_name() == null ||
                        queryUser.getEmail().trim().isEmpty() || queryUser.getPassword().trim().isEmpty() ||
                        queryUser.getFirst_name().trim().isEmpty() || queryUser.getLast_name().trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
                }

                //if user provides Id in request body, return bad request
                if(queryUser.getId() != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
                }

                //check for username email validation
                if(!userValidations.validateEmail(queryUser.getEmail())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
                }

                //retrieve user from db based on username
                User searchedUser = userService.getUserByEmail(queryUser.getEmail());

                //if user is present, return bad request else create user
                if(searchedUser == null) {
                    User newUser = new User();
                    //translate AddUser pojo to User pojo, save user and return details as json string
                    translateAddUserToUser(queryUser, newUser);
                    User savedUser = userService.save(newUser);
                    String jsonString = mapper.writeValueAsString(savedUser);
                    return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noCache()).contentType(MediaType.APPLICATION_JSON).body(jsonString);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }
    }

    //PUT Mapping-----------------------------------------------------------------------------------------------
    @PutMapping("/users")
    public ResponseEntity<Object> updateUser(@RequestParam(required = false) HashMap<String, String> param, @RequestHeader(required = false) HttpHeaders headers, @RequestBody(required = false) String userBody) {
        //if params are present or body is not present return bad request
        if(param.size() > 0 || userBody == null) {
//            logger.error("User Put error: Params are present or body is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }

        try {
            //get user credentials and authorize user
            String[] userCreds = getCreds(headers);

            //if user provides only username or password, or does not provides any credential, return bad request
            if(userCreds.length < 2) {
//                logger.error("User Put error: Enter both username and password");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
            if(!checkUserPassword) {
//                logger.error("User Put error: Unauthorized Access");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
            }

            //configure Jackson mapper and read request body json string
            ObjectMapper mapper = configureMapper();
            AddUser queryUser = mapper.readValue(userBody, AddUser.class);
            if(queryUser != null) {

                //the request body should contain atleast one of the below parameters
                if(queryUser.getPassword() == null || queryUser.getPassword().trim().isEmpty()) {
//                    warnLogger.warn("User Put Warning: Password is not present in body");
                    if(queryUser.getFirst_name() == null || queryUser.getFirst_name().trim().isEmpty()) {
//                        warnLogger.warn("User Put Warning: First Name is not present in body");
                        if(queryUser.getLast_name() == null || queryUser.getLast_name().trim().isEmpty()) {
//                            logger.error("User Put error: First Name, Last Name, Password fields are not present");
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
                        }
                    }
                }

                //if user submits username, id return bad request
                if(queryUser.getId() != null || queryUser.getEmail() != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
                }

                //retrieve user from database and update properties
                User user = userService.getUserByEmail(userCreds[0]);
                if(queryUser.getFirst_name() != null) user.setFirstName(queryUser.getFirst_name());
                if(queryUser.getLast_name() != null) user.setLastName(queryUser.getLast_name());
                if(queryUser.getPassword() != null) {
                    user.setPassword(queryUser.getPassword());
                } else {
                    user.setPassword(userCreds[1]);
                }
                User updatedUser = userService.save(user);

                if(updatedUser != null) {
//                    infoLogger.info("User Put Info: User Successfully Updated: " + updatedUser.getUsername());
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).cacheControl(CacheControl.noCache()).build();
                }
            } else {
//                logger.error("User Put error: Request Body Empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }
        } catch (JsonProcessingException e) {
//            logger.error("User Put error: " + e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
    }

    //get authorization credentials from header, decode base64 string, and return username, password seperately
    public String[] getCreds(org.springframework.http.HttpHeaders headers) {
        @SuppressWarnings("null")
        String authenticationToken = (headers != null &&
                headers.getFirst("authorization") != null) ?
                headers.getFirst("authorization").split(" ")[1] : "";

        byte[] decodeToken = Base64.getDecoder().decode(authenticationToken);
        String credentialString = new String(decodeToken, StandardCharsets.UTF_8);
        String[] credentials = !credentialString.isEmpty() ? credentialString.split(":") : new String[0];
        return credentials;
    }

    //configue Jackson mapper, with proper date time format
    public ObjectMapper configureMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        DateFormat format = DateFormat.getDateTimeInstance();
        mapper.setDateFormat(format);
        return mapper;
    }

    //convert AddUser object to User object
    private void translateAddUserToUser(AddUser addUser, User user) {
        user.setFirstName(addUser.getFirst_name());
        user.setLastName(addUser.getLast_name());
        user.setEmail(addUser.getEmail());
        user.setPassword(addUser.getPassword());
    }
}
