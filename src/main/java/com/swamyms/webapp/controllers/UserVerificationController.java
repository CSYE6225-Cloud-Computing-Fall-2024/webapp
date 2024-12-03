package com.swamyms.webapp.controllers;

import com.swamyms.webapp.entity.VerifyUser;
import com.swamyms.webapp.exceptionhandling.exceptions.UserNotFoundException;
import com.swamyms.webapp.service.VerifyUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/v2")
public class UserVerificationController {
    @Autowired
    private VerifyUserService verifyUserService;

    private static final Logger logger = LoggerFactory.getLogger(UserVerificationController.class);

    @GetMapping("/verify/{encodedUsername}")
    public ResponseEntity<Object> verifyUser(@RequestParam(required = false) HashMap<String, String> param, @PathVariable("encodedUsername") String encodedUsername, @RequestBody(required = false) String userBody) {


        Map<String, String> response = new HashMap<>();
    try{
        // Decode the username
        String username = new String(Base64.getUrlDecoder().decode(encodedUsername), StandardCharsets.UTF_8);
        logger.info("Getting User Info {}", username);

        // Check if params or body are present
        if(param.size() > 0 || userBody != null) {
            logger.error("Verify User Error: Params are present or body is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }

        VerifyUser verifyUser = verifyUserService.getByName(username);
        if(verifyUser.isVerified() == true) {
            logger.info("Verify User Info: User already verified");
            response.put("message", "Your email is already verified.");
            return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CacheControl.noCache()).body(response);
        }

        // Check if params or body are present
        if(param.size() > 0 || userBody != null) {
            logger.error("Verify User Error: Params are present or body is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }
        //if username is present, authenticate
        if(username != null) {
            logger.info("Verify User Info: Verifying user status");
            boolean userVerified = verifyUserService.updateStatus(username);
            if (userVerified) {
                logger.info("Verify User Info: User Verified");

                response.put("message", "User Verified Successfully !!");

                return ResponseEntity.status(HttpStatus.OK)
                        .cacheControl(CacheControl.noCache())
                        .body(response);
            } else {
                logger.info("Verify User Info: Link Expired");
                response.put("message", "Email verification unsuccessful. Your verification link expired after 2 minutes");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).cacheControl(CacheControl.noCache()).body(response);
            }
        }
        else {
            logger.error("Verify User Error : Username is not present in query");
        }

    } catch (UserNotFoundException e) {
        logger.error("Verify User Error: " + e.getMessage());
        response.put("error", "User not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).cacheControl(CacheControl.noCache()).body(response);
    } catch (IllegalArgumentException e) {
        logger.error("Verify User Error: Invalid encoded username");
        response.put("error", "Invalid username format.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).body(response);
    } catch (Exception e) {
        logger.error("Verify User Error: Unexpected error occurred", e);
        response.put("error", "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CacheControl.noCache()).body(response);
    }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
    }
}
