package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.*;

import java.util.Set;
import java.util.UUID;

public record MemberCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phoneNumber,
        String employeeId,
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotEmpty(message = "En az bir rol atanmalıdır.")
        Set<String> roles
) {
}
