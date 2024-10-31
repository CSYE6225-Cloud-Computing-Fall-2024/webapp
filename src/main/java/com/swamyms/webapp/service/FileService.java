    package com.swamyms.webapp.service;

    import com.swamyms.webapp.dao.FileRepository;
    import com.swamyms.webapp.entity.User;
    import com.swamyms.webapp.entity.file.FileEntity;
    import com.swamyms.webapp.entity.file.FileMapper;
    import com.swamyms.webapp.entity.file.model.FileUploadRequest;
    import com.swamyms.webapp.entity.file.model.FileUploadResponse;
    import com.swamyms.webapp.exceptionhandling.exceptions.ResourceNotFoundException;
    import io.micrometer.core.instrument.MeterRegistry;
    import io.micrometer.core.instrument.Timer;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;
    import software.amazon.awssdk.core.sync.RequestBody;
    import software.amazon.awssdk.services.s3.S3Client;
    import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
    import software.amazon.awssdk.services.s3.model.PutObjectRequest;

    import java.io.IOException;
    import java.net.URLEncoder;
    import java.nio.charset.StandardCharsets;


    @Service
    public class FileService {

        private final FileRepository fileRepository;
        private final FileMapper fileMapper;
        private final S3Client s3Client;

        @Value("${aws.s3.bucket.name}")
        private String bucketName;
        //Constructor Injection
        private final Logger logger = LoggerFactory.getLogger(FileService.class); // Logger instance
        private final MeterRegistry meterRegistry; // Meter registry for metrics

        private final Timer dbQueryTimer; // Timer for authenticateUser method

        public FileService(FileRepository theFileRepository, FileMapper theFileMapper, S3Client theS3Client, MeterRegistry meterRegistry){
            this.fileRepository = theFileRepository;
            this.fileMapper = theFileMapper;
            this.s3Client = theS3Client;
            this.meterRegistry = meterRegistry;


            this.dbQueryTimer = Timer.builder("db.image.queries.execution")
                    .description("Time taken for User database queries")
                    .register(meterRegistry);
        }

        public FileUploadResponse upload(FileUploadRequest fileUploadRequest, User user) throws IOException {
            long startTime = System.currentTimeMillis(); // Start timing API call
            logger.info("Profile Pic uploading service started user: {}", user.getEmail()); // Log saving user
            Timer.Sample sample = Timer.start(meterRegistry); // Start timing

            MultipartFile file = fileUploadRequest.file();
            String rawFileName = file.getOriginalFilename();
            String fileName = user.getId() + "/" + rawFileName;

            try{

                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(fileName)
                                .build(),
                        RequestBody.fromBytes(file.getBytes()));

                // Encode the file name for URL use
                String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8.toString());
                String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + user.getId() + "/" + encodedFileName;
//                String s3Url = bucketName+ "/" + user.getId() + "/" + encodedFileName;


                // Save file to local storage
//                Path filePath = Paths.get("uploads/" + fileName);
//                Files.createDirectories(filePath.getParent());
//                Files.write(filePath, file.getBytes());
//                String filePathString = filePath.toString();

                var fileEntity = fileMapper.toEntity(newID(), fileUploadRequest, user, s3Url);
                fileRepository.save(fileEntity);

                sample.stop(dbQueryTimer); // Stop timing and record
                logger.info("Image saved successfully in : {} ms", System.currentTimeMillis() - startTime); // Log success
                return fileMapper.toFileUploadResponse(fileEntity);
            }catch (Exception e) {
                logger.error("Image upload failed for user: {}", user.getEmail(), e); // Log error
                throw new RuntimeException("File upload failed: " + e.getMessage());
            }
        }


        public boolean getUserImageByUserID(String userID) {
            long startTime = System.currentTimeMillis(); // Start timing API call
            logger.info("Profile Pic fetched service started for user: {}", userID); // Log saving user
            Timer.Sample sample = Timer.start(meterRegistry); // Start timing

            boolean imageFetched = fileRepository.existsByUserId(userID);
            sample.stop(dbQueryTimer); // Stop timing and record
            logger.info("Profile Pic fetched successfully in : {} ms", System.currentTimeMillis() - startTime); // Log success
            return imageFetched;
        }

        public FileUploadResponse getImageDetailsByUserID(String userID){
            long startTime = System.currentTimeMillis(); // Start timing API call
            logger.info("Get Image Details service started for user: {}", userID); // Log saving user
            Timer.Sample sample = Timer.start(meterRegistry); // Start timing

            FileEntity fileEntity = fileRepository.findByUser_Id(userID);
            FileUploadResponse fileUploadResponse = fileMapper.toFileUploadResponse(fileEntity);

            sample.stop(dbQueryTimer); // Stop timing and record
            logger.info("Profile Pic fetched successfully in : {} ms", System.currentTimeMillis() - startTime); // Log success
            return fileUploadResponse;
        }

        public void deleteImageDetailsByUserID(String userID){

            long startTime = System.currentTimeMillis(); // Start timing API call
            logger.info("Delete Image Details service started for user: {}", userID); // Log saving user
            Timer.Sample sample = Timer.start(meterRegistry); // Start timing

            String fileName = fileRepository.findFileNameByUserId(userID);
            String key = userID + "/" + fileName;
            // Check if the file exists
            if (!fileRepository.existsByUserId(userID)) {
                throw new ResourceNotFoundException();
            }

            // Delete the object from S3
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            // If it exists, delete the user picture
            fileRepository.deleteByUserId(userID);

            sample.stop(dbQueryTimer); // Stop timing and record
            logger.info("Profile Pic deleted successfully in : {} ms", System.currentTimeMillis() - startTime);
//            fileRepository.deleteById(userID);
//            return fileMapper.toFileUploadResponse(fileRepository.findByUser_Id(userID));
        }


        private String newID(){
            return java.util.UUID.randomUUID().toString();
        }
    }
