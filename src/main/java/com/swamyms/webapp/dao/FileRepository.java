    package com.swamyms.webapp.dao;

    import com.swamyms.webapp.entity.file.FileEntity;
    import jakarta.transaction.Transactional;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Modifying;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface FileRepository extends JpaRepository<FileEntity, String> {
        boolean existsByUserId(String userID);

        @Query("SELECT f FROM FileEntity f WHERE f.user.id = :userId")
        FileEntity findByUser_Id(String userId);

        @Query("SELECT f.fileName FROM FileEntity f WHERE f.user.id = :userId")
        String findFileNameByUserId(String userId);

        // Add a delete method for user ID
        @Modifying
        @Transactional
        @Query("DELETE FROM FileEntity f WHERE f.user.id = :userId")
        void deleteByUserId(String userId);

    }
