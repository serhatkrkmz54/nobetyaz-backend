package com.paketnobet.nobetyaz.modules.organization.dto;

public record UserFromTokenResponse(
        String username,
        String firstName,
        String lastName
) {
}
