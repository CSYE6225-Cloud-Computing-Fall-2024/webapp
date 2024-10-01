package com.swamyms.webapp.dao;

import com.swamyms.webapp.entity.User;

import java.util.List;

public interface UserDAO {

    List<User> findAll();
}
