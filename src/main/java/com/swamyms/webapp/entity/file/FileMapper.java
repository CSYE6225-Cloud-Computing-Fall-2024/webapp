package com.swamyms.webapp.entity.file;

import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.entity.file.model.FileUploadRequest;
import com.swamyms.webapp.entity.file.model.FileUploadResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FileMapper {

    public FileEntity toEntity(String id, FileUploadRequest fileUploadRequest, User user, String filePath){
        return FileEntity.builder()
                .id(id)
                .fileName(fileUploadRequest.file().getOriginalFilename())
//                .size(fileUploadRequest.file().getSize())
                .url(filePath)
                .user(user)
                .uploadDate(LocalDate.now())  // Set the current date here
                .build();
    }

    public FileUploadResponse toFileUploadResponse(FileEntity fileEntity){
        return new FileUploadResponse(
                fileEntity.getId(),
                fileEntity.getFileName(),
                fileEntity.getUrl(),
//                fileEntity.getSize(),
                fileEntity.getUploadDate(),
                fileEntity.getUser().getId()
        );
    }
}
