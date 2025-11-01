package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record MemberUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phoneNumber,
        String employeeId,
        boolean isActive,
        UUID userId,
        List<UUID> qualificationIds
) {
}
