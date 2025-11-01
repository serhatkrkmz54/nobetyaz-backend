package com.paketnobet.nobetyaz.modules.organization.dto;

import java.time.LocalDate;

public record AdminDashboardStatsResponse(
        long totalActiveMembers,

        long shiftsOpen,
        long shiftsBidding,
        long shiftsConfirmed,

        long pendingShiftChanges,
        long pendingLeaveRequests,
        long pendingBids,

        LocalDate monthStartDate,
        LocalDate monthEndDate
) {
}
