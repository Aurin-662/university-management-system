package com.university.ums.security;

import com.university.ums.model.*;
import com.university.ums.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first admin so someone can log in and create other users. */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    @Value("${app.admin.username}") private String username;
    @Value("${app.admin.password}") private String password;

    @Override
    public void run(String... args) {
        if (repo.existsByRole(Role.ADMIN)) return;
        UserAccount a = new UserAccount();
        a.setUsername(username);
        a.setPassword(encoder.encode(password));
        a.setFullName("System Administrator");
        a.setEmail("admin@university.local");
        a.setRole(Role.ADMIN);
        repo.save(a);
    }
}
