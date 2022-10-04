package com.utility.utility_middleware.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MyUserDetailsService implements UserDetailsService {
    //from properties file
    @Value("${access.email}")
    private String USER_NAME;
    //from properties file
    @Value("${access.password}")
    private String PASSWORD;
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        //initiate username and password in the constractor
        //return  new User("user","password", new ArrayList<>());

        //use properties defined in applicaiton.properties file
        return  new User(USER_NAME,PASSWORD, new ArrayList<>());
    }
}
