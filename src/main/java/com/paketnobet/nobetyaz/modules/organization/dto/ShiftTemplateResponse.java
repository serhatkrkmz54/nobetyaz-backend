package com.paketnobet.nobetyaz.modules.organization.dto;

import java.time.LocalTime;
import java.util.UUID;

public record ShiftTemplateResponse(
        UUID id,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        double durationInHours,
        boolean isNightShift,
        boolean isActive
) {
}
