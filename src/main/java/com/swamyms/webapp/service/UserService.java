package com.swamyms.webapp.service;

import com.swamyms.webapp.entity.User;

public interface UserService {
    User save(User theUser);

    User getUserByEmail(String email);

    boolean authenticateUser(String email, String password);
}
