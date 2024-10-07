package com.swamyms.webapp.dao;

import com.swamyms.webapp.config.SecurityConfig;
import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.exceptionhandling.exceptions.DataBaseConnectionException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class UserDAOJpaImpl implements UserDAO {

    //define field for EntityManager
    private EntityManager entityManager;

    @Autowired
    private SecurityConfig securityConfig;

    //Constructor Injection
    @Autowired
    public UserDAOJpaImpl(EntityManager theEntityManager) {
        entityManager = theEntityManager;
    }
//    @Override
//    public List<User> findAll() {
//        //create a query
////        TypedQuery<User> theQuery = entityManager.createQuery("from User", User.class);
//
//        //execute a query and get the list of user
////        List<User> users = theQuery.getResultList();
//        //return the list of user
////        return users;
//        return null;
//    }

    @Override
    public User save(User theUser) {
        //save user if (id==0) then insert/save else update
        //return the dbUser
        try {
            theUser.setPassword(securityConfig.encodePassword(theUser.getPassword()));
            return entityManager.merge(theUser);
        } catch (PersistenceException e) {
            throw new DataBaseConnectionException();
        }

    }

    @Override
    public User findByEmail(String email) {
        //create a query
        try {
            String query = "SELECT u FROM User u WHERE u.email  = : email";
            TypedQuery<User> typedQuery = entityManager.createQuery(query, User.class);
            typedQuery.setParameter("email", email);
            return typedQuery.getSingleResult();
        }catch(NoResultException e){
            return null;
        }
        catch (PersistenceException e) {
            throw new DataBaseConnectionException();
        }
    }
}
