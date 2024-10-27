package com.swamyms.webapp.entity.file.model;

public record FileUploadResponse(
        String id,
        String fileName,
        String url
        ,
        Long size

) {
}
