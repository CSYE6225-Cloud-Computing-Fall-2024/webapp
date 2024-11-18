package com.swamyms.webapp.controllers;

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
@RequestMapping("/v1")
public class UserVerificationController {
    @Autowired
    private VerifyUserService verifyUserService;

    private static final Logger logger = LoggerFactory.getLogger(UserVerificationController.class);

    @GetMapping("/verify/{encodedUsername}")
    public ResponseEntity<Object> verifyUser(@RequestParam(required = false) HashMap<String, String> param, @PathVariable("encodedUsername") String encodedUsername, @RequestBody(required = false) String userBody) {
        Map<String, String> response = new HashMap<>();

        // Decode the username
        String username = new String(Base64.getUrlDecoder().decode(encodedUsername), StandardCharsets.UTF_8);
        logger.info("Getting User Info {}", username);

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

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
    }
}
