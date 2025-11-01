package com.paketnobet.nobetyaz.modules.organization.dto;

import java.util.UUID;

public record LocationResponse(
        UUID id,
        String name,
        String description,
        boolean isActive
) {
}
