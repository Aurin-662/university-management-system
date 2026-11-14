package com.university.ums.controller;

import com.university.ums.model.Role;
import com.university.ums.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
public class AdminController extends UserResourceController {
    public AdminController(UserService service) {
        super(service, Role.ADMIN);
    }
}
