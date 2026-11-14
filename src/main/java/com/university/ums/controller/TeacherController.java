package com.university.ums.controller;

import com.university.ums.model.Role;
import com.university.ums.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController extends UserResourceController {
    public TeacherController(UserService service) {
        super(service, Role.TEACHER);
    }
}
