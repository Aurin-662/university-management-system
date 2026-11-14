package com.university.ums.dto;

import com.university.ums.model.*;

public record UserResponse(Long id, String username, String fullName, String email,
                           Role role, String department, String studentId) {
    public static UserResponse from(UserAccount u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(),
                u.getRole(), u.getDepartment(), u.getStudentId());
    }
}
