package com.mxf.selaboj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private OrderService orderService;

    @Transactional
    public void createUser(String username) {
        System.out.println("Creating user: " + username);
        // 此处可能会涉及一些数据库操作...
        orderService.createOrder(username);
    }
}
