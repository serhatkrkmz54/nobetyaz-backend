package com.paketnobet.nobetyaz.modules.organization.dto;

import java.util.List;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String phoneNumber,
        String employeeId,
        boolean isActive,
        List<QualificationDTO> qualifications,
        String userStatus,
        String invitationToken,
        String username
) {
    public MemberResponse(UUID id, UUID userId, String firstName, String lastName,
                          String phoneNumber, String employeeId, boolean isActive,
                          List<QualificationDTO> qualifications) {
        this(id, userId, firstName, lastName, phoneNumber, employeeId, isActive, qualifications, null,null, null);
    }
}
