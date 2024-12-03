package com.swamyms.webapp.controllers;

import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.entity.VerifyUser;
import com.swamyms.webapp.entity.file.model.FileUploadRequest;
import com.swamyms.webapp.entity.file.model.FileUploadResponse;
import com.swamyms.webapp.service.FileService;
import com.swamyms.webapp.service.UserService;
import com.swamyms.webapp.service.VerifyUserService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("v2/user/self/pic")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private FileService fileService;
    private UserService userService;
    private MeterRegistry meterRegistry;
    private final Timer apiCallTimer;
    @Autowired
    VerifyUserService verifyUserService;

    @Autowired
    public FileController(FileService theFileService, UserService theUserService, MeterRegistry meterRegistry, VerifyUserService theVerifyUserService) {

        this.fileService = theFileService;
        this.userService = theUserService;
        this.meterRegistry = meterRegistry;
        this.verifyUserService=theVerifyUserService;


        // Register your metrics here
        this.apiCallTimer = Timer.builder("api.image.calls")
                .description("Time taken for user check API calls")
                .register(meterRegistry);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> uploadProfilePic(@RequestParam(required = false) HashMap<String, String> params,
                                                               @RequestHeader(required = false) HttpHeaders headers, @RequestBody(required = false) String requestBody,
                                                                @RequestParam(value = "profilePic", required = false) MultipartFile file,
                                                               HttpServletRequest request
                                                                ) {

        try{
            long startTime = System.currentTimeMillis(); // Start timing API call
            Timer.Sample sample = Timer.start(meterRegistry); // Start Timer for the API call

            if (params != null && !params.isEmpty()) {
                logger.error("Bad request: unexpected parameters");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }
            String requestContentType = request.getContentType();
            if (requestContentType == null || !requestContentType.startsWith("multipart/form-data")) {
                logger.error("Bad request: unexpected content type");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
//                        .body("Only 'multipart/form-data' content type is allowed");
            }
            if (requestBody != null && !requestBody.isEmpty()) {
                logger.error("Bad request: unexpected body present");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            if (file==null || file.getOriginalFilename().isEmpty()) {
                logger.error("Bad request: file not present");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }
            // Validate file type
            String contentType = file.getContentType();
            if (!isSupportedContentType(contentType)) {
                logger.error("Bad request: invalid content type");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
//                        .body("Bad Request: Unsupported file type. Only PNG, JPG, and JPEG files are allowed.");
            }
            //get user credentials from header and check authentication
            String[] userCreds = getCreds(headers);

            //if user provides only username or password, or does not provides any credential, return bad request
            if (userCreds.length < 2 || userCreds[0].isEmpty() || userCreds[1].isEmpty()) {
                logger.error("Bad request: invalid user credentials");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
            if (!checkUserPassword) {
                logger.error("Unauthorized request: invalid user credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
            }
            //check if user is verified
            VerifyUser verifyUser = verifyUserService.getByName(userCreds[0]);
            if(verifyUser.isVerified() != true) {
                logger.error("User Get Error: User not verified");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).cacheControl(CacheControl.noCache()).build();
            }

            //retrieve user from db
            User user = userService.getUserByEmail(userCreds[0]);

           boolean imageExists = fileService.getUserImageByUserID(user.getId());

           if(!imageExists){
               FileUploadRequest fileUploadRequest = new FileUploadRequest(file);
               FileUploadResponse fileUploadResponse = fileService.upload(fileUploadRequest, user);
               logger.info("Image uploaded successfully. Time taken: {} ms", System.currentTimeMillis() - startTime); // Log info
               sample.stop(apiCallTimer);
               return ResponseEntity.ok(fileUploadResponse);

           }else{
               logger.error("Bad request: Image already exists for user");
               return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CacheControl.noCache()).build();
    //                   .body("Profile pic already exists for User: " + user.getEmail());
           }
            //Check whether user has already uploaded a profile pic or not
                // if not uploaded let the user upload
                //if already uploaded I should handle this error
                //what error should be thrown
    //        FileUploadRequest fileUploadRequest = new FileUploadRequest(file, name);
    //        return ResponseEntity.ok(fileService.upload(fileUploadRequest, user));
            }catch (Exception e){
                logger.error("Internal Server error Post Request /user/self/pic: Exception connection : {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CacheControl.noCache()).build();
            }
    }


    @GetMapping
    public ResponseEntity<Object> getProfilePic(@RequestParam(required = false) HashMap<String, String> params,
                                                @RequestHeader(required = false) HttpHeaders headers,
                                                @RequestBody(required = false) String requestBody)
    {
        try{
            long startTime = System.currentTimeMillis(); // Start timing API call
            Timer.Sample sample = Timer.start(meterRegistry); // Start Timer for the API call
            if (params != null && !params.isEmpty()) {
                logger.error("Bad request: unexpected parameters");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            if (requestBody != null && !requestBody.isEmpty()) {
                logger.error("Bad request: unexpected body");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            //get user credentials from header and check authentication
            String[] userCreds = getCreds(headers);
            //if user provides only username or password, or does not provides any credential, return bad request
            if (userCreds.length < 2 || userCreds[0].isEmpty() || userCreds[1].isEmpty()) {
                logger.error("Bad request: invalid user credentials");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
            if (!checkUserPassword) {
                logger.error("Unauthorized request: invalid user credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
            }
            //check if user is verified
            VerifyUser verifyUser = verifyUserService.getByName(userCreds[0]);
            if(verifyUser.isVerified() != true) {
                logger.error("User Get Error: User not verified");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).cacheControl(CacheControl.noCache()).build();
            }

            //retrieve user from db
            User user = userService.getUserByEmail(userCreds[0]);

            boolean imageExists = fileService.
                    getUserImageByUserID(user.getId());

            if(imageExists){
                FileUploadResponse fileUploadResponse = fileService.getImageDetailsByUserID(user.getId());
                logger.info("Image fetched successfully. Time taken: {} ms", System.currentTimeMillis() - startTime); // Log info
                sample.stop(apiCallTimer);
                return ResponseEntity.ok(fileUploadResponse);
            }else{
                logger.error("Not request: Image does not exists for the user");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).cacheControl(CacheControl.noCache()).build();
//                        .body("Profile pic doesn't exists for User: " + user.getEmail());
            }
        }catch (Exception e){
            logger.error("Internal Server error Get Request /user/self/pic: Exception connection : {}", e.getMessage());
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

            long startTime = System.currentTimeMillis(); // Start timing API call
            Timer.Sample sample = Timer.start(meterRegistry); // Start Timer for the API call

            if (params != null && !params.isEmpty()) {
                logger.error("Bad request: unexpected parameters");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            if (requestBody != null && !requestBody.isEmpty()) {
                logger.error("Bad request: unexpected request body");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            //get user credentials from header and check authentication
            String[] userCreds = getCreds(headers);

            //if user provides only username or password, or does not provides any credential, return bad request
            if (userCreds.length < 2 || userCreds[0].isEmpty() || userCreds[1].isEmpty()) {
                logger.error("Bad request: user invalid credentials");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noCache()).build();
            }

            boolean checkUserPassword = userService.authenticateUser(userCreds[0], userCreds[1]);
            if (!checkUserPassword) {
                logger.error("Unauthorized request: user invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noCache()).build();
            }
            //check if user is verified
            VerifyUser verifyUser = verifyUserService.getByName(userCreds[0]);
            if(verifyUser.isVerified() != true) {
                logger.error("User Get Error: User not verified");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).cacheControl(CacheControl.noCache()).build();
            }
            //retrieve user from db
            User user = userService.getUserByEmail(userCreds[0]);

            boolean imageExists = fileService.
                    getUserImageByUserID(user.getId());

            if(imageExists){
                fileService.deleteImageDetailsByUserID(user.getId());
                logger.info("Image Deleted successfully. Time taken: {} ms", System.currentTimeMillis() - startTime); // Log info
                sample.stop(apiCallTimer);
                return ResponseEntity.noContent().build();
            }else{
                logger.error("Not Found request: Image doesn't exist to do PUT request /user/self/pic");
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
        logger.error("PUT /user/self/pic called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @PatchMapping
    private ResponseEntity<String> handlePatchMapping(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        logger.error("Patch /user/self/pic called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }

    @RequestMapping(method = RequestMethod.HEAD)
    private ResponseEntity<String>  handleHeadMapping(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        logger.error("Head /user/self/pic called, returning METHOD_NOT_ALLOWED.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
    }
    @RequestMapping(method = RequestMethod.OPTIONS)
    private ResponseEntity<String>  handleOptionsMapping(){
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        logger.error("Options /user/self/pic called, returning METHOD_NOT_ALLOWED.");
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
