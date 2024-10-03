package com.swamyms.webapp.dao;

import com.swamyms.webapp.entity.User;

public interface UserDAO {

    User save(User theUser);

    User findByEmail(String email);
}
