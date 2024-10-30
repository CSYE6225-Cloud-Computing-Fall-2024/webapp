    package com.swamyms.webapp.service;

    import com.swamyms.webapp.dao.FileRepository;
    import com.swamyms.webapp.entity.User;
    import com.swamyms.webapp.entity.file.FileMapper;
    import com.swamyms.webapp.entity.file.model.FileUploadRequest;
    import com.swamyms.webapp.entity.file.model.FileUploadResponse;
    import com.swamyms.webapp.exceptionhandling.exceptions.ResourceNotFoundException;
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

        public FileService(FileRepository theFileRepository, FileMapper theFileMapper, S3Client theS3Client){
            this.fileRepository = theFileRepository;
            this.fileMapper = theFileMapper;
            this.s3Client = theS3Client;
        }

        public FileUploadResponse upload(FileUploadRequest fileUploadRequest, User user) throws IOException {

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

                return fileMapper.toFileUploadResponse(fileEntity);
            }catch (Exception e) {
                throw new RuntimeException("File upload failed: " + e.getMessage());
            }
        }


        public boolean getUserImageByUserID(String userID) {
            return fileRepository.existsByUserId(userID);
        }

        public FileUploadResponse getImageDetailsByUserID(String userID){
            return fileMapper.toFileUploadResponse(fileRepository.findByUser_Id(userID));
        }

        public void deleteImageDetailsByUserID(String userID){

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
//            fileRepository.deleteById(userID);
//            return fileMapper.toFileUploadResponse(fileRepository.findByUser_Id(userID));
        }


        private String newID(){
            return java.util.UUID.randomUUID().toString();
        }
    }
