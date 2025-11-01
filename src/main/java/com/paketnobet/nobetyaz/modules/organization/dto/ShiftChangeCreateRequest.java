package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ShiftChangeCreateRequest(@NotNull UUID initiatingShiftId,
                                       @NotNull UUID targetShiftId,
                                       String reason) {
}
