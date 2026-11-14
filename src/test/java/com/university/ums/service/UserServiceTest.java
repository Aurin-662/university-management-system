package com.university.ums.service;

import com.university.ums.dto.UserRequest;
import com.university.ums.dto.UserResponse;
import com.university.ums.model.Role;
import com.university.ums.model.UserAccount;
import com.university.ums.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repo;
    @Mock PasswordEncoder encoder;
    @InjectMocks UserService service;

    // Unit test 1: create korle password hash hoy ar teacher save hoy
    @Test
    void create_hashesPasswordAndSavesTeacher() {
        UserRequest req = new UserRequest("t1", "pass123", "Dr. Rahman", "t1@uni.edu", "CSE", null);
        when(repo.findByUsername("t1")).thenReturn(Optional.empty());
        when(repo.findByEmail("t1@uni.edu")).thenReturn(Optional.empty());
        when(encoder.encode("pass123")).thenReturn("HASHED");
        when(repo.save(any(UserAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse res = service.create(Role.TEACHER, req);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repo).save(captor.capture());
        UserAccount saved = captor.getValue();
        assertEquals("HASHED", saved.getPassword());      // plain password na
        assertEquals(Role.TEACHER, saved.getRole());
        assertEquals("CSE", saved.getDepartment());
        assertEquals("t1", res.username());
    }

    // Unit test 2: password blank hole 400
    @Test
    void create_withBlankPassword_throwsBadRequest() {
        UserRequest req = new UserRequest("t1", "  ", "Dr. Rahman", "t1@uni.edu", "CSE", null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(Role.TEACHER, req));

        assertEquals(400, ex.getStatusCode().value());
        verify(repo, never()).save(any());
    }

    // Unit test 3: username age thakle 409
    @Test
    void create_withDuplicateUsername_throwsConflict() {
        UserAccount existing = new UserAccount();
        existing.setId(99L);
        when(repo.findByUsername("t1")).thenReturn(Optional.of(existing));
        UserRequest req = new UserRequest("t1", "pass123", "Dr. Rahman", "t1@uni.edu", "CSE", null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(Role.TEACHER, req));

        assertEquals(409, ex.getStatusCode().value());
        verify(repo, never()).save(any());
    }

    // Unit test 4: record na pele 404
    @Test
    void findById_whenNotFound_throwsNotFound() {
        when(repo.findByIdAndRole(5L, Role.TEACHER)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.findById(Role.TEACHER, 5L));

        assertEquals(404, ex.getStatusCode().value());
    }

    // Unit test 5: update e password na dile purano hash-i thake
    @Test
    void update_withoutPassword_keepsOldHash() {
        UserAccount existing = new UserAccount();
        existing.setId(2L);
        existing.setRole(Role.TEACHER);
        existing.setUsername("t1");
        existing.setPassword("OLD_HASH");
        when(repo.findByIdAndRole(2L, Role.TEACHER)).thenReturn(Optional.of(existing));
        when(repo.findByUsername("t1")).thenReturn(Optional.of(existing));
        when(repo.findByEmail("t1@uni.edu")).thenReturn(Optional.empty());
        when(repo.save(any(UserAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequest req = new UserRequest("t1", null, "Dr. Rahman Updated", "t1@uni.edu", "EEE", null);
        UserResponse res = service.update(Role.TEACHER, 2L, req);

        assertEquals("OLD_HASH", existing.getPassword());
        assertEquals("Dr. Rahman Updated", res.fullName());
        assertEquals("EEE", res.department());
        verify(encoder, never()).encode(any());
    }
}