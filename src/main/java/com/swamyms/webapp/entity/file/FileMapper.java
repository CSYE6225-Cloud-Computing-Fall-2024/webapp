package com.swamyms.webapp.entity.file;

import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.entity.file.model.FileUploadRequest;
import com.swamyms.webapp.entity.file.model.FileUploadResponse;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    public FileEntity toEntity(String id, FileUploadRequest fileUploadRequest, User user){
        return FileEntity.builder()
                .id(id)
                .fileName(fileUploadRequest.fileName())
                .size(fileUploadRequest.file().getSize())
                .user(user)
                .build();
    }

    public FileUploadResponse toFileUploadResponse(FileEntity fileEntity){
        return new FileUploadResponse(
                fileEntity.getId(),
                fileEntity.getFileName(),
                "",
                fileEntity.getSize()
        );
    }
}
