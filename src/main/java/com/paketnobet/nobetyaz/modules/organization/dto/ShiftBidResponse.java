package com.paketnobet.nobetyaz.modules.organization.dto;

import java.util.UUID;

public record ShiftBidResponse(UUID id, UUID memberId, String memberFullName, String status, String notes) {
}
