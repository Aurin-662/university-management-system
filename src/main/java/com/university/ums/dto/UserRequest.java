package com.university.ums.dto;

import jakarta.validation.constraints.*;

public record UserRequest(
        @NotBlank String username,
        String password, // required on create, optional on update
        @NotBlank String fullName,
        @NotBlank @Email String email,
        String department,
        String studentId) {
}
