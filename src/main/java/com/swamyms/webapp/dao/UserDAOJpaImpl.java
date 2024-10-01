package com.swamyms.webapp.dao;

import com.swamyms.webapp.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDAOJpaImpl implements UserDAO{

    //define field for EntityManager
    private EntityManager entityManager;

    //Constructor Injection
    @Autowired
    public UserDAOJpaImpl(EntityManager theEntityManager){
        entityManager = theEntityManager;
    }
    @Override
    public List<User> findAll() {
        //create a query
//        TypedQuery<User> theQuery = entityManager.createQuery("from User", User.class);

        //execute a query and get the list of user
//        List<User> users = theQuery.getResultList();
        //return the list of user
//        return users;
        return null;
    }
}
