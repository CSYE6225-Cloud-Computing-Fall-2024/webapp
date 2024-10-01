package com.swamyms.webapp.controllers;

import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class UserRestController {

    private UserService userService;
//    private UserDAO userDAO;
    //quick and dirty method inject userDAO(Use Constructor Injection)
    public UserRestController(UserService theUserService){
        userService = theUserService;
    }

    //expose "/users" and return list of users
    @GetMapping("/users")
    public List<User> findAll(){
        return userService.findAll();
    }

}
