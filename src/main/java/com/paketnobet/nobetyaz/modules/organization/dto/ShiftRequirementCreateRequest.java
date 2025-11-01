package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ShiftRequirementCreateRequest(
        @NotNull UUID locationId,
        @NotNull UUID shiftTemplateId,
        UUID qualificationId,
        @NotNull @Min(1) Integer requiredMemberCount
) {
}
