package com.paketnobet.nobetyaz.modules.organization.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ShiftInfo(UUID id, LocalDate date, LocalTime startTime, LocalTime endTime, String locationName) {
}
