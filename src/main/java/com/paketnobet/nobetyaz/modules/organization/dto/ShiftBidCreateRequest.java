package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.Size;

public record ShiftBidCreateRequest(
        @Size(max = 500) String notes
) {
}
