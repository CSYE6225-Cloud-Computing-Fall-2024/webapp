    package com.swamyms.webapp.dao;

    import com.swamyms.webapp.entity.file.FileEntity;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface FileRepository extends JpaRepository<FileEntity, String> {
        boolean existsByUserId(String userID);

        @Query("SELECT f FROM FileEntity f WHERE f.user.id = :userId")
        FileEntity findByUser_Id(String userId);

    }
