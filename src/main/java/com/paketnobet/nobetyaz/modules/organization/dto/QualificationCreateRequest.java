package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QualificationCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description
) {
}
