    package com.swamyms.webapp.service;

    import com.swamyms.webapp.dao.FileRepository;
    import com.swamyms.webapp.entity.User;
    import com.swamyms.webapp.entity.file.FileEntity;
    import com.swamyms.webapp.entity.file.FileMapper;
    import com.swamyms.webapp.entity.file.model.FileUploadRequest;
    import com.swamyms.webapp.entity.file.model.FileUploadResponse;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;
    import software.amazon.awssdk.core.sync.RequestBody;
    import software.amazon.awssdk.services.s3.S3Client;
    import software.amazon.awssdk.services.s3.model.PutObjectRequest;

    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;


    @Service
    public class FileService {

        private final FileRepository fileRepository;
        private final FileMapper fileMapper;
        private final S3Client s3Client;


        public FileService(FileRepository theFileRepository, FileMapper theFileMapper, S3Client theS3Client){
            this.fileRepository = theFileRepository;
            this.fileMapper = theFileMapper;
            this.s3Client = theS3Client;
        }

        public FileUploadResponse upload(FileUploadRequest fileUploadRequest, User user) throws IOException {

            MultipartFile file = fileUploadRequest.file();
            String fileName = user.getId() + "/" + file.getOriginalFilename(); // Customize path as needed

            // Define the S3 bucket name
            String bucketName = "SwamyMudigaS3Bucket"; // Replace with your actual bucket name



            try{
//                MultipartFile file = fileUploadRequest.file();
//
//                // Define file location
//                String fileName = user.getId() + "/" + file.getOriginalFilename();

                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(fileName)
                                .build(),
                        RequestBody.fromBytes(file.getBytes()));

                // Optionally, you can store the S3 URL for future reference
                String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;

//                String url = bucketName + fileName;

                // Save file to local storage or server (can replace this with cloud storage logic)
                Path filePath = Paths.get("uploads/" + fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, file.getBytes());
//                String filePathString = filePath.toString();
                var fileEntity = fileMapper.toEntity(newID(), fileUploadRequest, user, s3Url);
                fileRepository.save(fileEntity);

                return fileMapper.toFileUploadResponse(fileEntity);
            }catch (Exception e) {
                // Handle exceptions, e.g., log the error and throw a custom exception if needed
                throw new RuntimeException("File upload failed: " + e.getMessage());
            }
        }


        public boolean getUserImageByUserID(String userID) {
            return fileRepository.existsByUserId(userID);
        }

        public FileEntity getImageDetailsByUserID(String userID){
            return fileRepository.findByUser_Id(userID);
        }
//        User getUserByEmail(String email);


        private String newID(){
            return java.util.UUID.randomUUID().toString();
        }
    }
