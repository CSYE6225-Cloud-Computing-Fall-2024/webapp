package com.swamyms.webapp.service;

import com.swamyms.webapp.config.SecurityConfig;
import com.swamyms.webapp.dao.UserDAO;
import com.swamyms.webapp.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    //Inject UserDAO
    private UserDAO userDAO;

    @Autowired
    private SecurityConfig securityConfig;

    //Constructor Injection
    @Autowired
    public UserServiceImpl(UserDAO theUserDAO) {
        userDAO = theUserDAO;
    }
//    @Override
//    public List<User> findAll() {
//        return userDAO.findAll();
////        return null;
//    }

    @Transactional
    @Override
    public User save(User theUser) {
        return userDAO.save(theUser);
    }

    @Override
    public User getUserByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    public boolean authenticateUser(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user == null) return false;
        String dbPassword = user.getPassword();
        return securityConfig.authenticatePassword(password, dbPassword);
    }
}
