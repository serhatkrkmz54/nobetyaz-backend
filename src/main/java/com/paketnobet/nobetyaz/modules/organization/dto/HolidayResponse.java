package com.paketnobet.nobetyaz.modules.organization.dto;

import com.paketnobet.nobetyaz.modules.organization.model.enums.EDayType;

import java.time.LocalDate;
import java.util.UUID;

public record HolidayResponse(
        UUID id,
        String name,
        LocalDate holidayDate,
        EDayType holidayType
) {
}
