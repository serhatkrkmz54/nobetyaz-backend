package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotNull;

public record LeaveResolveRequest(
        @NotNull Boolean approved,
        String notes
) {
}
