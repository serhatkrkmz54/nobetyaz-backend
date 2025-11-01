package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record ShiftChangeActionRequest(@NotBlank String action, // "ACCEPT" veya "REJECT" / "APPROVE" veya "REJECT"
                                       String notes) {
}
