package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record LeaveRecordStatusUpdateRequest(@NotBlank String status) {
}
