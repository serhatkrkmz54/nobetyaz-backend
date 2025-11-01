package com.paketnobet.nobetyaz.modules.organization.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record MyBidResponse(
        UUID bidId,
        String bidStatus,
        String bidNotes,
        UUID shiftId,
        String locationName,
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime
) {
}
