package com.university.ums.integration;

import com.jayway.jsonpath.JsonPath;
import com.university.ums.model.Role;
import com.university.ums.model.UserAccount;
import com.university.ums.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UniversityApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository repo;
    @Autowired PasswordEncoder encoder;

    private static RequestPostProcessor admin()          { return user("admin").roles("ADMIN"); }
    private static RequestPostProcessor teacher(String u) { return user(u).roles("TEACHER"); }
    private static RequestPostProcessor student(String u) { return user(u).roles("STUDENT"); }

    // Protita test er age admin chara baki user muche dei, jate test gulo ek ar arekke bhanga na dey
    @BeforeEach
    void cleanUsers() {
        repo.deleteAll(repo.findAll().stream().filter(u -> u.getRole() != Role.ADMIN).toList());
    }

    private UserAccount saveUser(String username, Role role) {
        UserAccount u = new UserAccount();
        u.setUsername(username);
        u.setPassword(encoder.encode("pass123"));
        u.setFullName(username + " name");
        u.setEmail(username + "@uni.edu");
        u.setRole(role);
        return repo.save(u);
    }

    // Integration test 1: login (Security + DbUserDetailsService + BCrypt + Database)
    @Test
    void login_succeedsWithCorrectPassword_andFailsWithWrongOne() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .param("username", "admin").param("password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .param("username", "admin").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    // Integration test 2: admin teacher er puro CRUD (Controller + Service + JPA + Database)
    @Test
    void admin_canCreateReadUpdateAndDeleteTeacher() throws Exception {
        String createJson = """
                {"username":"it_teacher","password":"pass123","fullName":"Dr. Integration",
                 "email":"it_teacher@uni.edu","department":"CSE"}""";
        String updateJson = """
                {"username":"it_teacher","fullName":"Dr. Integration Updated",
                 "email":"it_teacher@uni.edu","department":"EEE"}""";

        // CREATE -> 201, response e password nei
        String body = mockMvc.perform(post("/api/teachers").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("it_teacher"))
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Number idNumber = JsonPath.read(body, "$.id");
        long id = idNumber.longValue();

        // Database e password hash hoye ache, plain text na
        UserAccount saved = repo.findByUsername("it_teacher").orElseThrow();
        assertNotEquals("pass123", saved.getPassword());
        assertTrue(encoder.matches("pass123", saved.getPassword()));

        // Same username abar -> 409
        mockMvc.perform(post("/api/teachers").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(createJson))
                .andExpect(status().isConflict());

        // READ -> 200
        mockMvc.perform(get("/api/teachers/" + id).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Integration"));

        // UPDATE -> 200
        mockMvc.perform(put("/api/teachers/" + id).with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("EEE"));

        // DELETE -> 204, tarpor 404
        mockMvc.perform(delete("/api/teachers/" + id).with(admin()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/teachers/" + id).with(admin()))
                .andExpect(status().isNotFound());
    }

    // Integration test 3: RBAC + ownership (401 / 403 / 200)
    @Test
    void rbacAndOwnershipRules_areEnforced() throws Exception {
        UserAccount t1 = saveUser("t1", Role.TEACHER);
        UserAccount t2 = saveUser("t2", Role.TEACHER);
        UserAccount s1 = saveUser("s1", Role.STUDENT);

        // Login chara -> 401
        mockMvc.perform(get("/api/teachers/" + t1.getId()))
                .andExpect(status().isUnauthorized());

        // Teacher nijer record -> 200
        mockMvc.perform(get("/api/teachers/" + t1.getId()).with(teacher("t1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("t1"));

        // Teacher onno teacher er record -> 403
        mockMvc.perform(get("/api/teachers/" + t2.getId()).with(teacher("t1")))
                .andExpect(status().isForbidden());

        // Teacher list dekhte pare na (shudhu admin) -> 403
        mockMvc.perform(get("/api/teachers").with(teacher("t1")))
                .andExpect(status().isForbidden());

        // Teacher student er API e pare na -> 403
        mockMvc.perform(get("/api/students/" + s1.getId()).with(teacher("t1")))
                .andExpect(status().isForbidden());

        // Teacher admin er API e pare na -> 403
        mockMvc.perform(get("/api/admins").with(teacher("t1")))
                .andExpect(status().isForbidden());

        // Student nijer record -> 200
        mockMvc.perform(get("/api/students/" + s1.getId()).with(student("s1")))
                .andExpect(status().isOk());
    }
}