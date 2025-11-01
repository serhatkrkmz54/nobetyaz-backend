package com.paketnobet.nobetyaz.modules.organization.dto;

import java.util.UUID;

public record QualificationResponse(
        UUID id,
        String name,
        String description
) {
}
