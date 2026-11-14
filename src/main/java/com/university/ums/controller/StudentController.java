package com.university.ums.controller;

import com.university.ums.model.Role;
import com.university.ums.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController extends UserResourceController {
    public StudentController(UserService service) {
        super(service, Role.STUDENT);
    }
}
