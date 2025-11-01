package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MemberPreferenceUpdateRequest(
        @NotNull(message = "Tercih puanı boş olamaz.")
        @Min(value = -10, message = "Puan en az -10 olabilir.")
        @Max(value = 10, message = "Puan en fazla +10 olabilir.")
        int preferenceScore
) {
}
