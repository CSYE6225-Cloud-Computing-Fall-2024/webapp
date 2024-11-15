package com.swamyms.webapp.dao;

import com.swamyms.webapp.entity.VerifyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifyUserDAO extends JpaRepository<VerifyUser, String> {

    VerifyUser findByUsername(String email);
}
