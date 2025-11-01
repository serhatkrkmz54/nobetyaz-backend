package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRecordUpdateRequest(@NotBlank String leaveType,
                                       @NotNull @FutureOrPresent LocalDate startDate,
                                       @NotNull @FutureOrPresent LocalDate endDate,
                                       String reason,
                                       @NotBlank String status) {
}
