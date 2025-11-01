package com.paketnobet.nobetyaz.modules.organization.dto;

import com.paketnobet.nobetyaz.modules.organization.model.enums.EDayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayCreateRequest(
        @NotBlank(message = "Tatil adı boş olamaz")
        String name,

        @NotNull(message = "Tarih boş olamaz")
        LocalDate holidayDate,

        @NotNull(message = "Tatil tipi boş olamaz")
        EDayType holidayType
) {
}
