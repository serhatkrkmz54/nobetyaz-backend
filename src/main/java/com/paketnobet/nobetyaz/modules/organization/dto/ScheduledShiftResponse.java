package com.paketnobet.nobetyaz.modules.organization.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduledShiftResponse(
        UUID id,
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime,
        String status,
        LocationInfo location,
        MemberInfo member,
        ShiftTemplateInfo shiftTemplate,
        QualificationInfo requiredQualification
) {
    public record LocationInfo(UUID id, String name) {}
    public record MemberInfo(UUID id, String firstName, String lastName, UUID userId) {}
    public record ShiftTemplateInfo(UUID id, String name) {}
    public record QualificationInfo(UUID id, String name) {}
}