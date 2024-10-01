package com.swamyms.webapp.service;

import com.swamyms.webapp.dao.UserDAO;
import com.swamyms.webapp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    //Inject UserDAO
    private UserDAO userDAO;

    //Constructor Injection
    @Autowired
    public UserServiceImpl(UserDAO theUserDAO){
        userDAO = theUserDAO;
    }
    @Override
    public List<User> findAll() {
        return userDAO.findAll();
//        return null;
    }
}
