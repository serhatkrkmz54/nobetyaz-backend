package com.paketnobet.nobetyaz.modules.organization.dto;

import java.util.UUID;

public record MemberMonthlySummaryResponse(UUID memberId,
                                           String memberFullName,
                                           int year,
                                           int month,
                                           double totalScheduledHours,
                                           long totalLeaveDays) {
}
