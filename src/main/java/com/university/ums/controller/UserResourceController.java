package com.university.ums.controller;

import com.university.ums.dto.*;
import com.university.ums.model.Role;
import com.university.ums.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Shared CRUD logic; each subclass fixes the role and base path.
 * Admins can do everything. Teachers/students can only read, update
 * or delete their own record (URL-level role checks are in SecurityConfig).
 */
public abstract class UserResourceController {
    private static final String ADMIN_OR_SELF = "hasRole('ADMIN') or @access.isSelf(#id, authentication)";

    private final UserService service;
    private final Role role;

    protected UserResourceController(UserService service, Role role) {
        this.service = service;
        this.role = role;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        return service.findAll(role);
    }

    @GetMapping("/{id}")
    @PreAuthorize(ADMIN_OR_SELF)
    public UserResponse getOne(@PathVariable Long id) {
        return service.findById(role, id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest req) {
        UserResponse created = service.create(role, req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_OR_SELF)
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest req) {
        return service.update(role, id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_OR_SELF)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(role, id);
        return ResponseEntity.noContent().build();
    }
}
