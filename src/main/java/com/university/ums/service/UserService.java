package com.university.ums.service;

import com.university.ums.dto.*;
import com.university.ums.model.*;
import com.university.ums.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(Role role) {
        return repo.findByRole(role).stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Role role, Long id) {
        return UserResponse.from(get(role, id));
    }

    @Transactional
    public UserResponse create(Role role, UserRequest r) {
        if (r.password() == null || r.password().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        checkUnique(r, null);
        UserAccount u = new UserAccount();
        u.setRole(role);
        apply(u, r);
        u.setPassword(encoder.encode(r.password()));
        return UserResponse.from(repo.save(u));
    }

    @Transactional
    public UserResponse update(Role role, Long id, UserRequest r) {
        UserAccount u = get(role, id);
        checkUnique(r, id);
        apply(u, r);
        if (r.password() != null && !r.password().isBlank())
            u.setPassword(encoder.encode(r.password()));
        return UserResponse.from(repo.save(u));
    }

    @Transactional
    public void delete(Role role, Long id) {
        repo.delete(get(role, id));
    }

    private UserAccount get(Role role, Long id) {
        return repo.findByIdAndRole(id, role).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, role + " not found with id " + id));
    }

    private void apply(UserAccount u, UserRequest r) {
        u.setUsername(r.username());
        u.setFullName(r.fullName());
        u.setEmail(r.email());
        u.setDepartment(u.getRole() == Role.TEACHER ? r.department() : null);
        u.setStudentId(u.getRole() == Role.STUDENT ? r.studentId() : null);
    }

    private void checkUnique(UserRequest r, Long selfId) {
        repo.findByUsername(r.username()).filter(o -> !o.getId().equals(selfId)).ifPresent(o -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        });
        repo.findByEmail(r.email()).filter(o -> !o.getId().equals(selfId)).ifPresent(o -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        });
    }
}
