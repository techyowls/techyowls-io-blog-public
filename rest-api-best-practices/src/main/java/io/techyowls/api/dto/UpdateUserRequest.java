package io.techyowls.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
    @NotBlank String name,
    @Email String email,
    @Min(18) int age
) {}
