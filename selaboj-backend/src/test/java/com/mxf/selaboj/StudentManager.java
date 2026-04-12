package com.mxf.selaboj;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudentManager {
    @Autowired
    private Student student;

    public void show() {
        System.out.println(student.getId());
        System.out.println(student.getName());
    }
}