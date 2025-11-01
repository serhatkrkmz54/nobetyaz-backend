package com.paketnobet.nobetyaz.modules.dto;

import java.util.List;
import java.util.UUID;

public record JwtResponse(String token, UUID id, String username, String firstName,  String lastName, String email,
                          String phoneNumber, List<String> roles, boolean showOnboarding) {
}
