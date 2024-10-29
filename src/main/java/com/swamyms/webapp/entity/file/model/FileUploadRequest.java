package com.swamyms.webapp.entity.file.model;

import org.springframework.web.multipart.MultipartFile;

public record FileUploadRequest(MultipartFile file)
{
}
