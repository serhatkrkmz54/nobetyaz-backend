package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationCreateRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 100) String description,
        boolean isActive
) {}
