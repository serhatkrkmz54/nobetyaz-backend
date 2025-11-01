package com.paketnobet.nobetyaz.modules.organization.dto;

import com.paketnobet.nobetyaz.modules.organization.model.enums.ELeaveStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestResponse(
        UUID id,
        MemberInfo member,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        ELeaveStatus status,
        Instant createdAt
) {
    public record MemberInfo(
            UUID id,
            String firstName,
            String lastName,
            String employeeId,
            String phoneNumber
    ) {}
}
