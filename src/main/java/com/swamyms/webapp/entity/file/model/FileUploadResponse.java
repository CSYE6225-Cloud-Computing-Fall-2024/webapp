package com.swamyms.webapp.entity.file.model;

import java.time.LocalDate;

public record FileUploadResponse(
        String id,
        String fileName,
        String url,
//        Long size,
        LocalDate uploadDate,

        String UserID

) {
}
