package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MemberPreferenceCreateRequest(
        @NotNull(message = "Nöbet şablonu ID'si boş olamaz.")
        UUID shiftTemplateId,

        @NotNull(message = "Haftanın günü boş olamaz.")
        @Min(value = 1, message = "Gün 1-7 (Pzt-Pzr) arasında olmalıdır.")
        @Max(value = 7, message = "Gün 1-7 (Pzt-Pzr) arasında olmalıdır.")
        int dayOfWeek,

        @NotNull(message = "Tercih puanı boş olamaz.")
        @Min(value = -10, message = "Puan en az -10 olabilir.")
        @Max(value = 10, message = "Puan en fazla +10 olabilir.")
        int preferenceScore
) {
}
