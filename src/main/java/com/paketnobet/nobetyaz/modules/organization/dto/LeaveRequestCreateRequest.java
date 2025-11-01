package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRequestCreateRequest(
        @NotBlank(message = "İzin tipi boş olamaz")
        String leaveType,

        @NotNull(message = "Başlangıç tarihi zorunludur")
        @FutureOrPresent(message = "Başlangıç tarihi geçmiş bir tarih olamaz")
        LocalDate startDate,

        @NotNull(message = "Bitiş tarihi zorunludur")
        @FutureOrPresent(message = "Bitiş tarihi geçmiş bir tarih olamaz")
        LocalDate endDate,

        String reason
) {
}
