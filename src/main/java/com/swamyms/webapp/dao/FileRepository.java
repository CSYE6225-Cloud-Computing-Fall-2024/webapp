    package com.swamyms.webapp.dao;

    import com.swamyms.webapp.entity.file.FileEntity;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface FileRepository extends JpaRepository<FileEntity, String> {
    }
