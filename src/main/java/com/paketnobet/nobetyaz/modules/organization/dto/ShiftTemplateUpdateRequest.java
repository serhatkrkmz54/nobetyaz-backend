package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalTime;

public record ShiftTemplateUpdateRequest(
        @NotBlank String name,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        boolean isNightShift,
        @NotNull Boolean isActive
) {
}
