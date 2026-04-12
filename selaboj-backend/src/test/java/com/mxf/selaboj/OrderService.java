package com.mxf.selaboj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private UserService userService;

    @Transactional
    public void createOrder(String username) {
        System.out.println("Creating order for user: " + username);
        //...
    }
}
