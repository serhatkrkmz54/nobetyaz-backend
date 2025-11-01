package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignMemberRequest(@NotNull UUID memberId) {
}
