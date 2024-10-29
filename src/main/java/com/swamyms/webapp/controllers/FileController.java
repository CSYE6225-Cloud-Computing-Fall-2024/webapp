package com.swamyms.webapp.controllers;

import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.entity.file.model.FileUploadRequest;
import com.swamyms.webapp.service.FileService;
import com.swamyms.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<Object> uploadProfilePic(@RequestParam(required = false) HashMap<String, String> params,
                                                               @RequestHeader(required = false) HttpHeaders headers, @RequestBody(required = false) String requestBody,
                                                                @RequestParam(value = "profilePic", required = false) MultipartFile file,
                                                               HttpServletRequest request
                                                                ) {

        try{

            if (params != null && !params.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }
            String requestContentType = request.getContentType();
            if (requestContentType == null || !requestContentType.startsWith("multipart/form-data")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
//                        .body("Only 'multipart/form-data' content type is allowed");
            }
            if (requestBody != null && !requestBody.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            if (file==null || file.getOriginalFilename().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).body("GetMapping User error: Params are present or body is not null");
            }
            // Validate file type
            String contentType = file.getContentType();
            if (!isSupportedContentType(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
//                        .body("Bad Request: Unsupported file type. Only PNG, JPG, and JPEG files are allowed.");
            }
        //get user credentials from header and check authentication
        String[] userCreds = getCreds(headers);

        //if user provides only username or password, or does not provides any credential, return bad request
        if (userCreds.length < 2 || userCreds[0].isEmpty() || userCreds[1].isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
        }

        boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
        if (!checkUserPassword) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
        }

        //retrieve user from db
        User user = userService.getUserByEmail(userCreds[0]);

       boolean imageExists = fileService.getUserImageByUserID(user.getId());

       if(!imageExists){
           FileUploadRequest fileUploadRequest = new FileUploadRequest(file);
           return ResponseEntity.ok(fileService.upload(fileUploadRequest, user));
       }else{
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
//                   .body("Profile pic already exists for User: " + user.getEmail());
       }
        //Check whether user has already uploaded a profile pic or not
            // if not uploaded let the user upload
            //if already uploaded I should handle this error
            //what error should be thrown
//        FileUploadRequest fileUploadRequest = new FileUploadRequest(file, name);
//        return ResponseEntity.ok(fileService.upload(fileUploadRequest, user));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CacheControl.noCache()).build();
        }
    }


    @GetMapping
    public ResponseEntity<Object> getProfilePic(@RequestParam(required = false) HashMap<String, String> params,
                                                @RequestHeader(required = false) HttpHeaders headers,
                                                @RequestBody(required = false) String requestBody)
    {
        try{
            if (params != null && !params.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            if (requestBody != null && !requestBody.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            //get user credentials from header and check authentication
            String[] userCreds = getCreds(headers);

            //if user provides only username or password, or does not provides any credential, return bad request
            if (userCreds.length < 2 || userCreds[0].isEmpty() || userCreds[1].isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
            if (!checkUserPassword) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
            }

            //retrieve user from db
            User user = userService.getUserByEmail(userCreds[0]);

            boolean imageExists = fileService.
                    getUserImageByUserID(user.getId());

            if(imageExists){
                return ResponseEntity.ok(fileService.getImageDetailsByUserID(user.getId()));
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).cacheControl(CacheControl.noCache()).build();
//                        .body("Profile pic doesn't exists for User: " + user.getEmail());
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CacheControl.noCache()).build();
        }
    }


    @GetMapping("/")
    public ResponseEntity<?> getProfilePicWithSlash(@RequestParam(required = false) HashMap<String, String> params,
                                                    @RequestHeader(required = false) HttpHeaders headers,
                                                    @RequestBody(required = false) String requestBody) {
        return getProfilePic(params, headers, requestBody); // You can redirect or just return the same response
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteProfilePic(@RequestParam(required = false) HashMap<String, String> params,
                                                @RequestHeader(required = false) HttpHeaders headers,
                                                @RequestBody(required = false) String requestBody)
    {
        try{
            if (params != null && !params.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            if (requestBody != null && !requestBody.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            //get user credentials from header and check authentication
            String[] userCreds = getCreds(headers);

            //if user provides only username or password, or does not provides any credential, return bad request
            if (userCreds.length < 2 || userCreds[0].isEmpty() || userCreds[1].isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
            if (!checkUserPassword) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
            }

            //retrieve user from db
            User user = userService.getUserByEmail(userCreds[0]);

            boolean imageExists = fileService.
                    getUserImageByUserID(user.getId());

            if(imageExists){
                fileService.deleteImageDetailsByUserID(user.getId());
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).cacheControl(CacheControl.noCache()).build();
//                        .body("Profile pic doesn't exists therefore can't be deleted for User: " + user.getEmail());
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CacheControl.noCache()).build();
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteProfilePicWithSlash(@RequestParam(required = false) HashMap<String, String> params,
                                                    @RequestHeader(required = false) HttpHeaders headers,
                                                    @RequestBody(required = false) String requestBody) {
        return deleteProfilePic(params, headers, requestBody); // You can redirect or just return the same response
    }

    @PutMapping
    private ResponseEntity<String> handlePutMapping(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @PatchMapping
    private ResponseEntity<String> handlePatchMapping(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @RequestMapping(method = RequestMethod.HEAD)
    private ResponseEntity<String>  handleHeadMapping(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }
    @RequestMapping(method = RequestMethod.OPTIONS)
    private ResponseEntity<String>  handleOptionsMapping(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }


    public String[] getCreds(org.springframework.http.HttpHeaders headers) {
        @SuppressWarnings("null") String authenticationToken = (headers != null && headers.getFirst("authorization") != null) ? headers.getFirst("authorization").split(" ")[1] : "";

        byte[] decodeToken = Base64.getDecoder().decode(authenticationToken);
        String credentialString = new String(decodeToken, StandardCharsets.UTF_8);
        String[] credentials = !credentialString.isEmpty() ? credentialString.split(":") : new String[0];
        return credentials;
    }


    // Helper method to validate the content type
    private boolean isSupportedContentType(String contentType) {
        return contentType != null && (
                contentType.equals(MediaType.IMAGE_PNG_VALUE) ||
                contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
                contentType.equals("image/jpg") || // Some browsers use "image/jpg" instead of "image/jpeg"
                contentType.equals("image/heic") || // Apple HEIC format
                contentType.equals("image/heif")    // HEIF format
        );
    }

}
