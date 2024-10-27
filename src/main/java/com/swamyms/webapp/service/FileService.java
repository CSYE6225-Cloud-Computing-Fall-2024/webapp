    package com.swamyms.webapp.service;

    import com.swamyms.webapp.dao.FileRepository;
    import com.swamyms.webapp.entity.User;
    import com.swamyms.webapp.entity.file.FileMapper;
    import com.swamyms.webapp.entity.file.model.FileUploadRequest;
    import com.swamyms.webapp.entity.file.model.FileUploadResponse;
    import org.springframework.stereotype.Service;

    @Service
    public class FileService {

        private final FileRepository fileRepository;
        private final FileMapper fileMapper;


        public FileService(FileRepository theFileRepository, FileMapper theFileMapper){
            this.fileRepository = theFileRepository;
            this.fileMapper = theFileMapper;
        }

        public FileUploadResponse upload(FileUploadRequest fileUploadRequest, User user){
            var fileEntity = fileMapper.toEntity(newID(), fileUploadRequest, user);
            fileRepository.save(fileEntity);

            return fileMapper.toFileUploadResponse(fileEntity);

        }
        private String newID(){
            return java.util.UUID.randomUUID().toString();
        }
    }
