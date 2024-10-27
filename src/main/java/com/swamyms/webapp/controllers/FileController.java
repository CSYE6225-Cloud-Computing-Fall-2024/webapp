package com.swamyms.webapp.controllers;

import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.service.FileService;
import com.swamyms.webapp.entity.file.model.FileUploadRequest;
import com.swamyms.webapp.entity.file.model.FileUploadResponse;
import com.swamyms.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

@Validated
@RestController
@RequestMapping("v1/user/self/pic")
public class FileController {

    private FileService fileService;
    private UserService userService;

    @Autowired
    public FileController(FileService theFileService, UserService theUserService) {

        this.fileService = theFileService;
        this.userService = theUserService;
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FileUploadResponse> uploadProfilePic(@RequestParam(required = false) HashMap<String, String> params,
                                                               @RequestHeader(required = false) HttpHeaders headers, @RequestBody(required = false) String requestBody,
                                                               @Validated @RequestParam("file") MultipartFile file,
                                                               @RequestParam("fileName") String name) {


        //if params are present or body is not present return bad request
//        if ((params != null && !params.isEmpty()) || (requestBody != null && !requestBody.isEmpty())) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).body("GetMapping User error: Params are present or body is not null");
//        }

        try{
        //get user credentials from header and check authentication
        String[] userCreds = getCreds(headers);

        //if user provides only username or password, or does not provides any credential, return bad request
        if (userCreds.length < 2 || userCreds[0].isEmpty() || userCreds[1].isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).body("GetMapping User error: Enter both username and password for Basic Auth");
        }

        boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
        if (!checkUserPassword) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).body("GetMapping User error: Unauthorized Access");
        }

        //retrieve user from db
        User user = userService.getUserByEmail(userCreds[0]);

        FileUploadRequest fileUploadRequest = new FileUploadRequest(file, name);
        return ResponseEntity.ok(fileService.upload(fileUploadRequest, user));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }
    }

    public String[] getCreds(org.springframework.http.HttpHeaders headers) {
        @SuppressWarnings("null") String authenticationToken = (headers != null && headers.getFirst("authorization") != null) ? headers.getFirst("authorization").split(" ")[1] : "";

        byte[] decodeToken = Base64.getDecoder().decode(authenticationToken);
        String credentialString = new String(decodeToken, StandardCharsets.UTF_8);
        String[] credentials = !credentialString.isEmpty() ? credentialString.split(":") : new String[0];
        return credentials;
    }
}
